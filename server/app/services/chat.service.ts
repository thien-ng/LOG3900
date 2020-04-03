import { injectable, inject } from "inversify";
import Types from '../types';
import { IUser } from "../interfaces/user-manager";
import { IChannelIds, IReceptMes, IEmitMes, IChannelMessageReq, ISearchChannel } from "../interfaces/chat";
import { IUserId } from '../interfaces/user-manager';
import { ChatDbService } from "../database/chat-db.service";
import { IStatus, IInvitationChannel, IInviteFriend, IChannelParticipation } from '../interfaces/communication';
import * as pg from "pg";
import * as io from 'socket.io';
import { Time } from '../utils/date';
import { UserManagerService } from "./user-manager.service";

@injectable()
export class ChatService {

    private socket: io.Server;

    // for sending messages to specific channels
    private channelMapUsersList: Map<string, IUser[]>;

    // for saving conversations in db
    private usernameMapUserId: Map<string, number>;

    private usernameMapSocketId: Map<string, string>;

    public constructor(
            @inject(Types.ChatDbService)      private db:       ChatDbService,
            @inject(Types.UserManagerService) private userServ: UserManagerService) {
        this.channelMapUsersList = new Map<string, IUser[]>();
        this.usernameMapUserId = new Map<string, number>();
        this.usernameMapSocketId = new Map<string, string>();
    }

    public setSocket(socket: io.Server): void {
        this.socket = socket;
    }

    public async addUserToChannelMap(user: IUser): Promise<void> {
        const name: string = user.username;

        const channelList: IChannelIds[] = (await this.db.getChannelsWithAccountName(name)).rows.map((row: any) => ({ id: row.channel_id }));
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

        this.getUserId(name).then((id: number) => { this.usernameMapUserId.set(name, id) });
        this.usernameMapSocketId.set(user.username, user.socketId);
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
        this.usernameMapSocketId.delete(username);
    }

    private getUserId(username: string): Promise<number> {
        return this.db.getAccountIdByUsername(username).then((result: pg.QueryResult) => {
            const res: IUserId[] = result.rows.map((row: any) => ({ id: row.id }));
            return res[0].id;
        });
    }

    public sendMessages(mes: IReceptMes): void {

        const currTime = Time.now();

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
        } else {
            throw new Error(`cannnot find user list from ${mes.channel_id}`);
        }

        // save message to DB
        this.db.insertChannelMessage({
            channel_id: mes.channel_id,
            account_id: this.usernameMapUserId.get(mes.username) as number,
            content: mes.content,
            time: currTime,
        });
    }

    public async getMessagesWithChannelId(id: string): Promise<IStatus | IChannelMessageReq[]> {
        if (this.verifyName(id)) {
            return {
                status: 400,
                message: "channel id length should be between 1 and 20",
            }
        }

        const result: pg.QueryResult = await this.db.getMessagesWithChannelId(id);
        const messages: IChannelMessageReq[] = result.rows.map((row: any) => (
            {
                username: row.out_username,
                content: row.out_content,
                time: row.out_times,
            }
        ));
        return messages;
    }

    public async getChannelsWithAccountName(username: string): Promise<IStatus | IChannelIds[]> {
        if (this.verifyName(username)) {
            return {
                status: 400,
                message: "username length should be between 1 and 20",
            }
        }

        const result: pg.QueryResult = await this.db.getChannelsWithAccountName(username);
        const channels: IChannelIds[] = result.rows.map((row: any) => ({ id: row.channel_id }));
        return channels;
    }

    public async joinChannel(join: IChannelParticipation): Promise<IStatus> {
        let result = this.buildReturnStatus(`Successfully joined ${join.channel}`);

        try {
            this.verifyParticipationChannel(join);
            const subbedChannels = (await this.db.getChannelsWithAccountName(join.username)).rows.map((row: any) => ({ id: row.channel_id }));

            if (subbedChannels.some(chan => { return chan.id === join.channel }))
                throw new Error(`${join.username} is already subscribed to ${join.channel}.`);

            const isNew = (await this.db.joinChannel(join.username, join.channel)).rows[0].joinchannel;
            const users = this.userServ.getUsers();
            if (isNew === 1) {
                users.forEach(u => {
                    if (u.username !== join.username)
                        this.socket.to(u.socketId).emit("channel-new", {id: join.channel});
                });
            }
            
            this.updateUserToChannels(join.username, join.channel, true);
        } catch (e) {
            result.status = 400
            result.message = e.message;
        }

        return result;
    }

    public async leaveChannel(leave: IChannelParticipation): Promise<IStatus> {
        let result = this.buildReturnStatus(`Successfully left ${leave.channel}`);

        try {
            this.verifyParticipationChannel(leave);

            const subbedChannels = (await this.db.getChannelsWithAccountName(leave.username)).rows.map((row: any) => ({ id: row.channel_id }));

            if (!subbedChannels.some(chan => chan.id === leave.channel))
                throw new Error(`${leave.username} is not subscribed to ${leave.channel}.`);
            if (leave.channel === "general")
                throw new Error(`cannot leave default channel: ${leave.channel}.`);

            await this.db.leaveChannel(leave.username, leave.channel);
            this.updateUserToChannels(leave.username, leave.channel, false);
        } catch (e) {
            result.status = 400
            result.message = e.message;
        }

        return result;
    }

    private updateUserToChannels(username: string, channel: string, isJoin: boolean) {
        const socketId = this.usernameMapSocketId.get(username);
        if (!socketId) { throw new Error(`${username} not found in connected list`) }

        if (isJoin)
            this.joinChannelMap(channel, { username: username, socketId: socketId });
        else
            this.leaveChannelMap(channel, username);

        this.socket.emit("channel-update");
    }

    private joinChannelMap(channel: string, user: IUser): void {
        const userList = this.channelMapUsersList.get(channel);

        if (userList) {
            userList.push(user);
            this.channelMapUsersList.set(channel, userList);
        } else
            this.channelMapUsersList.set(channel, [user]);
    }

    private leaveChannelMap(channel: string, username: string): void {
        const userList = this.channelMapUsersList.get(channel);

        if (userList) {
            const newList = userList.filter(u => u.username !== username);
            this.channelMapUsersList.set(channel, newList);
        }
    }

    public async getChannelsNotSubWithAccountName(username: string): Promise<IChannelIds[]> {
        return (await this.db.getChannelsNotSubWithAccountName(username)).rows.map((row: any) => ({ id: row.id }));
    }

    public async getSearChannelsByName(username: string, word: string = ""): Promise<ISearchChannel[]> {
        return (await this.db.getSearChannelsByName(username, word)).rows.map((row: any) => (
            {
                id: row.out_channel,
                sub: this.convertBoolToString(row.sub)
            }));
    }

    private convertBoolToString(bool: string): boolean {
        return bool === 'true'
    }

    public async sendInviteToChannel(invit: IInviteFriend): Promise<IStatus> {

        let result = this.buildReturnStatus("Invitation sent successfully");
        const socketId = this.usernameMapSocketId.get(invit.invitee);

        try {
            this.verifyInvittaion(invit);
            if (!socketId) {
                throw new Error(`cannot find ${invit.invitee}'s socketID`);
            }
            const joinResult = await this.joinChannel({ username: invit.invitee, channel: invit.channel });

            if (joinResult.status === 400) {
                result = joinResult
            } else {
                this.socket.to(socketId).emit("invitation", {
                    message: `${invit.inviter} has invited you to channel ${invit.channel}.`,
                    channel: invit.channel,
                } as IInvitationChannel);
            }

        } catch (e) {
            result.status = 400;
            result.message = e.message;
        }

        return result;
    }

    private verifyInvittaion(invit: IInviteFriend): void {
        if (invit.invitee.length < 1 || invit.invitee.length > 20) {
            throw new Error("invitee name length should be between 1 and 20");
        }
        if (invit.inviter.length < 1 || invit.inviter.length > 20) {
            throw new Error("inviter name length should be between 1 and 20");
        }
        if (invit.channel.length < 1 || invit.channel.length > 20) {
            throw new Error("channel id length should be between 1 and 20");
        }
    }

    private verifyParticipationChannel(particip: IChannelParticipation): void {
        if (particip.username.length < 1 || particip.username.length > 20) {
            throw new Error("username length should be between 1 and 20");
        }
        if (particip.channel.length < 1 || particip.channel.length > 20) {
            throw new Error("channel id length should be between 1 and 20");
        }
        if (particip.channel.toLocaleLowerCase().trim() === "lobby" ||
            particip.channel.toLocaleLowerCase().trim() === "game") {
            throw new Error("channel name cannot be 'lobby' or 'game'");
        }
    }

    private verifyName(name: string): boolean {
        return (name.length < 1 || name.length > 20)
    }

    private buildReturnStatus(message: string): IStatus {
        return { status: 200, message: message }
    }

}
