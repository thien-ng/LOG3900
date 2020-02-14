import { injectable, inject } from "inversify";
import Types from '../types';
import { IUser } from "../interfaces/user-manager";
import * as pg from "pg";
import * as io from 'socket.io';
import { IChannelIds, IReceptMes, IEmitMes, IChannelMessageReq } from "../interfaces/chat";
import { IUserId } from '../interfaces/user-manager';
import { ChatDbService } from "../database/chat-db.service";
import { IStatus } from '../interfaces/communication';

@injectable()
export class ChatService {

    private socket: io.Server;

    // for sending messages to specific channels
    private channelMapUsersList: Map<string, IUser[]>;

    // for saving conversations in db
    private usernameMapUserId: Map<string, number>;

    public constructor(@inject(Types.ChatDbService) private db: ChatDbService) {
        this.channelMapUsersList = new Map<string, IUser[]>();
        this.usernameMapUserId = new Map<string, number>();
    }

    public setSocket(socket: io.Server): void {
        this.socket = socket;
    }

    public addUserToChannelMap(user: IUser): void {
        const name: string = user.username;
        
        this.db.getChannelsWithAccountName(name).then((result: pg.QueryResult) => {
            const channelList: IChannelIds[] = result.rows.map((row: any) => ({id: row.channel_id}));
            
            channelList.forEach((chan: IChannelIds) => {
                let list: IUser[] | undefined = this.channelMapUsersList.get(chan.id);
                
                if (list) {
                    list.push(user);
                } else {
                    list = [];
                    list.push(user);
                }
                
                this.channelMapUsersList.set(chan.id, list);              
            });
        });

        this.getUserId(name).then((id: number) => {this.usernameMapUserId.set(name, id)});
    }

    public removeUserFromChannelMap(username: string): void {
        const newList = new Map<string, IUser[]>()

        this.channelMapUsersList.forEach((list: IUser[], key: string) => {
            const filteredList = list.filter(user => user.username != username);
            if (filteredList.length !== 0) {
                newList.set(key, filteredList);
            }
        });

        this.channelMapUsersList = newList
    }

    private getUserId(username: string): Promise<number> {
        return this.db.getAccountIdByUsername(username).then((result: pg.QueryResult) => {
            const res: IUserId[] = result.rows.map((row: any) => ({id: row.id}));
            return res[0].id;
        });
    }

    public sendMessages(mes: IReceptMes): void {

        const currTime = this.convertDateTemplate();
        
        const mesToSend: IEmitMes = {
            username: mes.username,
            channel_id: mes.channel_id,
            content: mes.content,
            time: currTime,
        }
        
        // send to everyone in channel
        const list: IUser[] | undefined = this.channelMapUsersList.get(mes.channel_id);

        if (list) {
            list.forEach((user: IUser) => {
                this.socket.to(user.socketId).emit("chat", mesToSend);
            });
        }

        // save message to DB
        this.db.insertChannelMessage({
            channel_id: mes.channel_id,
            account_id: this.usernameMapUserId.get(mes.username) as number,
            content:    mes.content,
            time:       currTime,
        });
    }

    private convertDateTemplate(): string {
        const today = new Date();
        const hour: string = this.formatTime(today.getHours());
        const minute: string = this.formatTime(today.getMinutes());
        const second: string = this.formatTime(today.getSeconds());

        return hour + ":" + minute + ":" + second;
    }

    private formatTime(time: number): string {
        return time > 9 ? time.toString() : `0${time}`;
    }

    public async getMessagesWithChannelId(id: string): Promise<void | IChannelMessageReq[]> {
        const result: pg.QueryResult = await this.db.getMessagesWithChannelId(id);
        const messages: IChannelMessageReq[] = result.rows.map((row: any) => (
            {
                username: row.out_username,
                content:  row.out_content,
                time:     row.out_times,
            }
        ));
        return messages;
    }

    public async getChannelsWithAccountName(username: string): Promise<void | IChannelIds[]> {
        const result: pg.QueryResult = await this.db.getChannelsWithAccountName(username);
        const channels: IChannelIds[] = result.rows.map((row: any) => ({id: row.channel_id}));
        return channels;
    }

    public async joinChannel(username: string, channel: string): Promise<IStatus> {
        const subbedChannels = (await this.db.getChannelsWithAccountName(username)).rows.map((row: any) => ({id: row.channel_id}));
        const account_id = this.usernameMapUserId.get(username);
        let result: IStatus = {
            status: 200,
            message: `Successfully joined ${channel}`,
        };
        try {
            if (!account_id)
                throw new Error(`${username} not found in usernameMapUserId`);
            if (subbedChannels.some(chan => chan.id === channel))
                throw new Error(`${username} is already subscribed to ${channel}.`);

            await this.db.joinChannel(account_id, channel);
        } catch(e) {
            result.status = 400
            result.message = e.message;
        }

        return result;
    }

    public async leaveChannel(username: string, channel: string): Promise<IStatus> {
        const subbedChannels = (await this.db.getChannelsWithAccountName(username)).rows.map((row: any) => ({id: row.channel_id}));
        const account_id = this.usernameMapUserId.get(username);
        let result: IStatus = {
            status: 200,
            message: `Successfully left ${channel}`,
        };
        try {
            if (!account_id)
                throw new Error(`${username} not found in usernameMapUserId`);
            if (!subbedChannels.some(chan => chan.id === channel))
                throw new Error(`${username} is not subscribed to ${channel}.`);
            if (channel === "general")
                throw new Error(`cannot leave default channel: ${channel}.`);
    
            await this.db.leaveChannel(account_id, channel);
        } catch(e) {
            result.status = 400
            result.message = e.message;
        }

        return result;
    }


}