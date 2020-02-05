import { injectable, inject } from "inversify";
import { DatabaseService } from "./database.service";
import Types from '../types';
import { IUser } from "../interfaces/user-manager";
import * as pg from "pg";
import * as io from 'socket.io';
import { IChannelIds, IReceptMes, IEmitMes } from "../interfaces/chat";
import { IUserId } from '../interfaces/user-manager';

@injectable()
export class ChatService {

    private socket: io.Server;

    // for sending messages to specific channels
    private channelMapUsersList: Map<number, IUser[]>;

    // for saving conversations in db
    private usernameMapUserId: Map<string, number>;

    public constructor(@inject(Types.DatabaseService) private db: DatabaseService) {
        this.channelMapUsersList = new Map<number, IUser[]>();
        this.usernameMapUserId = new Map<string, number>();
    }

    public setSocket(socket: io.Server): void {
        this.socket = socket;
    }

    public addUserToChannelMap(user: IUser): void {
        const name: string = user.username;
        
        this.db.getChannelsWithAccountId(name).then((result: pg.QueryResult) => {
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
        const newList = new Map<number, IUser[]>()

        this.channelMapUsersList.forEach((list: IUser[], key: number) => {
            newList.set(key, list.filter(user => user.username != username));
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

        const currTime: Date = new Date();

        
        const mesToSend: IEmitMes = {
            username: mes.username,
            channel_id: mes.channel_id,
            content: mes.content,
            time: this.convertDateTemplate(currTime),
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
        });
    }

    private convertDateTemplate(today: Date): string {
        const hour: string = today.getHours().toString();
        const minute: string = today.getMinutes().toString();
        const second: string = today.getSeconds().toString();

        return hour + ":" + minute + ":" + second;
    }


}