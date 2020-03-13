import { injectable, inject } from "inversify";
import { IJoinLobby, ILeaveLobby, IActiveLobby, IReceptMesLob, INotify, LobbyNotif, INotifyUpdateUser , INotifyLobbyUpdate, IGetLobby } from "../../interfaces/game";
import { IUser } from "../../interfaces/user-manager";
import { UserManagerService } from "../user-manager.service";
import { isUuid } from 'uuidv4';

import Types from '../../types';
import * as io from 'socket.io';


@injectable()
export class LobbyManagerService {

    public lobbies: Map<string , IActiveLobby>

    private socketServer: io.Server;

    public constructor(@inject(Types.UserManagerService) private userServ: UserManagerService) {
        this.lobbies = new Map<string, IActiveLobby>();
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

    public getActiveLobbies(gameID: string): IGetLobby[] {
        const list: IGetLobby[] = [];
        this.lobbies.forEach((lob) => {
            if (lob.gameID === gameID)
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
            private: lobbyAct.private,
            size: lobbyAct.size,
            lobbyName: lobbyAct.lobbyName,
            gameID: lobbyAct.gameID,
        }; 
    }

    public join(req: IJoinLobby): string {
        this.verifyRequest(req);

        const user = this.userServ.getUsersByName(req.username);

        if (!user)  throw new Error(`${req.username} is not found in logged users`);

        const lobby = this.lobbyDoesExists(req.lobbyName);

        if (lobby) {
            
            // Join lobby            
            if (this.isUserInLobbyAlready(lobby.users, user.username))
                throw new Error(`${user.username} is already in lobby ${lobby.lobbyName}`);

            if (lobby.private && this.isPwdMatching(req.password as string, lobby.password as string)) {
                if ((lobby.users.length + 1) > lobby.size)
                    throw new Error(`Max number of users in lobby ${lobby.lobbyName} reached`);

                lobby.users.push(user);
                this.sendMessages({lobbyName: lobby.lobbyName, type: LobbyNotif.join, user: user});
            }
            else if (lobby.private == false){
                lobby.users.push(user);
                this.sendMessages({lobbyName: lobby.lobbyName, type: LobbyNotif.join, user: user});
            }
            else
                throw new Error(`Wrong password for lobby ${req.lobbyName}`);
        } else {

            // Create Lobby
            if (!req.size)
                throw new Error("Lobby size must be specified when lobby does not exist")
            if (!req.gameID || (req.gameID && !isUuid(req.gameID)))
                throw new Error("UUID attribute must be an UUID");
           
            this.lobbies.set(req.lobbyName, {users: [user], private: req.private, size: req.size, password: req.password, lobbyName: req.lobbyName, gameID: req.gameID} as IActiveLobby);
            this.sendMessages({lobbyName: req.lobbyName, type: LobbyNotif.create, users: [user], private: req.private, size: req.size} as INotifyLobbyUpdate);          
        }        

        return `Successfully joined lobby ${req.lobbyName}`;
    }

    public leave(req: ILeaveLobby): string {
        this.verifyRequest(req);

        const user = this.userServ.getUsersByName(req.username);

        if (!user)  throw new Error(`${req.username} is not found in logged users`);

        const lobby = this.lobbyDoesExists(req.lobbyName);
        
        if (lobby) {
            lobby.users = lobby.users.filter(u => {return u.username !== req.username});
            if (lobby.users.length === 0) {
                // Delete lobby
                this.lobbies.delete(req.lobbyName);
                this.sendMessages({lobbyName: req.lobbyName, type: LobbyNotif.delete});
            } else {
                // Leave lobby
                this.lobbies.set(req.lobbyName, lobby);
                this.sendMessages({lobbyName: req.lobbyName, type: LobbyNotif.leave});
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
        this.leave({username: username, lobbyName: lobbyName});
    }

    public sendMessages(mes: IReceptMesLob | INotifyUpdateUser | INotifyLobbyUpdate): void {
        const lobby = this.lobbyDoesExists(mes.lobbyName);

        if (!lobby) return;

        if (this.isNotification(mes))
            lobby.users.forEach(u => { this.socketServer.to(u.socketId).emit("lobby-notif") });
        else 
            lobby.users.forEach(u => { this.socketServer.to(u.socketId).emit("lobby-chat", mes.message) });
    }

    private isNotification(object: any): object is INotify {
        return "type" in object;
    }

    private isUserInLobbyAlready(users: IUser[], name: string): boolean {
        return users.some(u => {return u.username === name});
    }

    private isPwdMatching(pw: string, lobbyPw: string): boolean {
        return pw === lobbyPw;
    }

    private lobbyDoesExists(lobbyName: string): IActiveLobby | undefined {
        return this.lobbies.get(lobbyName);
    }

    private verifyRequest(req: IJoinLobby | ILeaveLobby): void {
        this.verifySocketConnection();

        if (req.username.length < 1 || req.username.length > 20)
            throw new Error("Username lenght must be between 1 and 20");
        if (req.lobbyName.length < 1 || req.lobbyName.length > 20)
            throw new Error("Lobby name must be between 1 and 20");
        
        if (!this.isJoinLobby(req))
            return;
        if (req.size || req.size === 0) 
            if (req.size < 1 || req.size > 10)
                throw new Error("Lobby size should be between 1 and 10");
        if (typeof req.private !== "boolean")
            throw new Error("Private attribute must be boolean");
        if (req.private)
            if (!req.password)
                throw new Error("Private lobby must have password");
            else if (req.password.length < 1 || req.password.length > 20)
                throw new Error("Password lenght must be between 1 and 20");
    }

    private isJoinLobby(req: IJoinLobby | ILeaveLobby): req is IJoinLobby {
        return "private" in req;
    }

    private verifySocketConnection(): void {
        if (!this.socketServer)
            throw new Error("Socket is not connected");
    }

}