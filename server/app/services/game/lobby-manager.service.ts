import { injectable, inject } from "inversify";
import { IJoinLobby, ILeaveLobby, IActiveLobby, IReceptMesLob, INotify, LobbyNotif, INotifyUpdateUser, INotifyLobbyUpdate, IGetLobby, GameMode, ILobEmitMes, Bot } from "../../interfaces/game";
import { IUser } from "../../interfaces/user-manager";
import { UserManagerService } from "../user-manager.service";
import { Time } from "../../utils/date";

import Types from '../../types';
import * as io from 'socket.io';


@injectable()
export class LobbyManagerService {

    public lobbies: Map<string, IActiveLobby>;
    public lobbiesMessages: Map<string, IReceptMesLob[]>;

    private socketServer: io.Server;

    public constructor(@inject(Types.UserManagerService) private userServ: UserManagerService) {
        this.lobbies = new Map<string, IActiveLobby>();
        this.lobbiesMessages = new Map<string, IReceptMesLob[]>();
    }

    public initSocketServer(socketServer: io.Server): void {
        this.socketServer = socketServer;
    }

    public getUsersInLobby(lobbyName: string): string[] {
        const lobby = this.lobbies.get(lobbyName) as IActiveLobby;
        const users: string[] = [];
        lobby.users.forEach(u => { users.push(u.username) });
        return users;
    }

    public getMessagesByLobbyName(lobbyName: string): IReceptMesLob[] {
        const messages = this.lobbiesMessages.get(lobbyName);
        return (messages) ? messages : [];
    }

    public getActiveLobbies(mode: GameMode): IGetLobby[] {
        const list: IGetLobby[] = [];
        this.lobbies.forEach((lob) => {
            if (lob.mode === mode)
                list.push(this.mapLobby(lob));
        })
        return list;
    }

    private mapLobby(lobbyAct: IActiveLobby): IGetLobby {
        const usernames: string[] = [];
        lobbyAct.users.forEach(u => {
            usernames.push(u.username);
        });
        return {
            usernames: usernames,
            isPrivate: lobbyAct.isPrivate,
            size: lobbyAct.size,
            lobbyName: lobbyAct.lobbyName,
            mode: lobbyAct.mode,
        };
    }

    public invite(lobbyName: string, username: string): string {
        this.verifyLobbyUsernameLength(username, lobbyName);

        const user = this.userServ.getUsersByName(username);

        if (!user) throw new Error(`${username} is not found in logged users`);

        const lobby = this.lobbyDoesExists(lobbyName);

        if (!lobby) throw new Error(`lobby ${lobbyName} doesn't exist`);

        if (lobby.whitelist && !this.isUserWhitelisted(lobby, user)) {
            lobby.whitelist.push(user);
        }
        else {
            lobby.whitelist = [user];
        }
        this.socketServer.to(user.socketId).emit("lobby-invitation", { type: LobbyNotif.invitation, lobbyName: lobbyName })
        return `${username} added to whitelist`;
    }

    public removeWhitelist(lobbyName: string, username: string): string {
        this.verifyLobbyUsernameLength(username, lobbyName);

        const user = this.userServ.getUsersByName(username);

        if (!user) throw new Error(`${username} is not found in logged users`);

        const lobby = this.lobbyDoesExists(lobbyName);

        if (!lobby) throw new Error(`lobby ${lobbyName} doesn't exist`);

        if (lobby.whitelist) {
            lobby.whitelist = lobby.whitelist.filter((user) => {
                return user.username != username;
            });
        }
        return `${username} removed from whitelist`;
    }

    private isUserWhitelisted(lobby: IActiveLobby, user: IUser): boolean {
        return (lobby != undefined) && (lobby.whitelist != undefined) && (lobby.whitelist.find((item) => {
            item == user;
        }) == undefined);
    }

    public join(req: IJoinLobby): string {
        this.verifyRequest(req);

        let user: IUser | undefined = { username: req.username, socketId: "" };
        const isBot: boolean = this.isBot(req.username)
        if (!isBot)
            user = this.userServ.getUsersByName(req.username);

        if (!user) throw new Error(`${req.username} is not found in logged users`);

        const lobby = this.lobbyDoesExists(req.lobbyName);

        if (lobby) {

            // Join lobby
            if (lobby.users.length > lobby.size - 1) {
                throw new Error("Maximum size of user in lobby reached");
            }
            if (this.isUserInLobbyAlready(lobby.users, user.username))
                throw new Error(`${user.username} is already in lobby ${lobby.lobbyName}`);

            if (lobby.isPrivate && (this.isPwdMatching(req.password as string, lobby.password as string) || this.isUserWhitelisted(lobby, user)) || isBot) {
                lobby.users.push(user);
                this.sendMessages({ lobbyName: lobby.lobbyName, type: LobbyNotif.join, username: user.username, mode: lobby.mode } as INotifyUpdateUser);
            }
            else if (lobby.isPrivate == false) {
                lobby.users.push(user);
                this.sendMessages({ lobbyName: lobby.lobbyName, type: LobbyNotif.join, username: user.username, mode: lobby.mode } as INotifyUpdateUser);
            }
            else
                throw new Error(`Wrong password for lobby ${req.lobbyName}`);
        } else {

            // Create Lobby
            if (isBot)
                throw new Error("A bot cannot create a lobby");
            if (!req.size)
                throw new Error("Lobby size must be specified when lobby does not exist")
            if (!req.mode || (req.mode && !(req.mode in GameMode))) {
                throw new Error("Creating lobby must have correct mode");
            }

            this.lobbies.set(req.lobbyName, { users: [user], isPrivate: req.isPrivate, size: req.size, password: req.password, lobbyName: req.lobbyName, mode: req.mode } as IActiveLobby);
            this.lobbiesMessages.set(req.lobbyName, []);
            this.sendMessages({ lobbyName: req.lobbyName, type: LobbyNotif.create, usernames: [user.username], isPrivate: req.isPrivate, size: req.size, mode: req.mode } as INotifyLobbyUpdate);
        }

        return `Successfully joined lobby ${req.lobbyName}`;
    }

    public leave(req: ILeaveLobby): string {
        this.verifyRequest(req);

        let user: IUser | undefined = { username: req.username, socketId: "" };
        if (!this.isBot(req.username))
            user = this.userServ.getUsersByName(req.username);

        if (!user) throw new Error(`${req.username} is not found in logged users`);

        const lobby = this.lobbyDoesExists(req.lobbyName);

        if (lobby) {
            if (req.isKicked) {
                const user = this.findUserToKick(lobby.users, req.username);
                if (user)
                    this.socketServer.to(user.socketId).emit("lobby-kicked");
            }

            lobby.users = lobby.users.filter(u => { return u.username !== req.username });
            const mode = lobby.mode;

            if (this.checkUsersLeftExceptBot(lobby)) {
                // Delete lobby
                this.sendMessages({ lobbyName: req.lobbyName, type: LobbyNotif.delete, mode: mode });
                this.lobbies.delete(req.lobbyName);
                this.lobbiesMessages.delete(req.lobbyName);
            } else {
                // Leave lobby
                this.lobbies.set(req.lobbyName, lobby);
                this.sendMessages({ lobbyName: req.lobbyName, type: LobbyNotif.leave, username: req.username, mode: mode });
            }

        } else {
            throw new Error(`${req.lobbyName} not found`);
        }
        return `Left ${req.lobbyName} successfully`;
    }

    public handleDisconnect(username: string): void {
        let lobbyName: string | undefined;

        this.lobbies.forEach(lob => {
            lob.users.forEach(user => {
                if (user.username === username)
                    lobbyName = lob.lobbyName;
            });
        });
        if (!lobbyName)
            return
        this.leave({ username: username, lobbyName: lobbyName, isKicked: false });
    }

    public sendMessages(mes: IReceptMesLob | INotifyUpdateUser | INotifyLobbyUpdate): void {
        const lobby = this.lobbyDoesExists(mes.lobbyName);

        if (!lobby) return;

        if (this.isNotification(mes))
            this.socketServer.emit("lobby-notif", mes);
        else {
            const message = { lobbyName: mes.lobbyName, username: mes.username, content: mes.content, time: Time.now() } as ILobEmitMes;
            lobby.users.forEach(u => { this.socketServer.to(u.socketId).emit("lobby-chat", message) });

            const messages: IReceptMesLob[] = this.lobbiesMessages.get(mes.lobbyName) as IReceptMesLob[];
            messages.push(mes);
            this.lobbiesMessages.set(mes.lobbyName, messages);
        }
    }

    private checkUsersLeftExceptBot(lobby: IActiveLobby): boolean {
        let count = 0;
        lobby.users.forEach(u => {
            if (!this.isBot(u.username))
                count++;
        });
        return count === 0;
    }

    private isNotification(object: any): object is INotify {
        return "type" in object;
    }

    private isUserInLobbyAlready(users: IUser[], name: string): boolean {
        return users.some(u => { return u.username === name });
    }

    private isPwdMatching(pw: string, lobbyPw: string): boolean {
        return pw === lobbyPw;
    }

    private lobbyDoesExists(lobbyName: string): IActiveLobby | undefined {
        return this.lobbies.get(lobbyName);
    }

    private verifyLobbyUsernameLength(username: string, lobbyName: string): void {
        if (!this.isBot(username)) {
            if (!username || (username.length < 1 || username.length > 20))
                throw new Error("Username lenght must be between 1 and 20");
            if (!/^[a-zA-Z0-9]+$/.test(username))
                throw new Error("Username must be alphanumeric");
        }
        if (!lobbyName || lobbyName.length < 1 || lobbyName.length > 20)
            throw new Error("Lobby name must be between 1 and 20");
    }

    private verifyRequest(req: IJoinLobby | ILeaveLobby): void {
        this.verifySocketConnection();
        this.verifyLobbyUsernameLength(req.username, req.lobbyName);
        if (!this.isJoinLobby(req))
            return;

        const jointReq = req as IJoinLobby;

        if (jointReq.size || jointReq.size === 0)
            if (jointReq.size < 1 || jointReq.size > 10)
                throw new Error("Lobby size should be between 1 and 10");
        if ((jointReq.isPrivate == undefined) || typeof jointReq.isPrivate !== "boolean")
            throw new Error("Private attribute must be boolean");
        if (jointReq.isPrivate)
            if (!jointReq.password)
                throw new Error("Private lobby must have password");
            else if (jointReq.password.length < 1 || jointReq.password.length > 20)
                throw new Error("Password lenght must be between 1 and 20");
    }

    private isBot(username: string): boolean {
        let isBotName = false;
        if (username === Bot.humour) isBotName = true;
        if (username === Bot.kind) isBotName = true;
        if (username === Bot.mean) isBotName = true;
        return isBotName;
    }

    private isJoinLobby(req: IJoinLobby | ILeaveLobby): boolean {
        return Object.keys(req).length >= 4;
    }

    private verifySocketConnection(): void {
        if (!this.socketServer)
            throw new Error("Socket is not connected");
    }

    private findUserToKick(users: IUser[], playerKicked: string): IUser | undefined {
        return users.find(u => { return u.username === playerKicked });
    }

}
