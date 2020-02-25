import { injectable, inject } from "inversify";
import { IJoinLobby, ILeaveLobby, IActiveLobby, IReceptMes } from "../../interfaces/game";
import { IUser } from "../../interfaces/user-manager";
import { UserManagerService } from "../user-manager.service";

import * as io from 'socket.io';
import Types from '../../types';

@injectable()
export class LobbyManagerService {

    private lobbies: Map<string , IActiveLobby>
    private socketServer:  io.Server;

    public constructor(@inject(Types.UserManagerService) private userServ: UserManagerService) {
        this.lobbies = new Map<string, IActiveLobby>();
    }

    public initSocketServer(socketServer: io.Server): void {
        this.socketServer = socketServer;     
    }

    public sendMessages(mes: IReceptMes): void {
        const lobby = this.lobbyDoesExists(mes.lobbyName);

        if (lobby) {
            lobby.users.forEach(u => {
                this.socketServer.to(u.socketId).emit(mes.message);
            });
        }
    }

    public getActiveLobbies(): IActiveLobby[] {
        const list: IActiveLobby[] = [];
        this.lobbies.forEach((val) => {
            list.push(val);
        })
        return list;
    }

    public join(req: IJoinLobby): string {
        this.verifyRequest(req);

        const user = this.userServ.getUsersByName(req.username);

        if (!user)  throw new Error(`${req.username} is not found in logged users`);

        const lobby = this.lobbyDoesExists(req.lobbyName);

        if (lobby) {
            if (lobby.private && this.isPwdMatching(req.password as string, lobby.password as string)) {
                if (this.isUserInLobbyAlready(lobby.users, user.username))
                    throw new Error(`${user.username} is already in lobby ${lobby.lobbyName}`);
                if ((lobby.users.length + 1) > lobby.size)
                    throw new Error(`Max number of users in lobby ${lobby.lobbyName} reached`);
                lobby.users.push(user);
            }
            else if (lobby.private == false)
                lobby.users.push(user);
            else
                throw new Error(`Wrong password for lobby ${req.lobbyName}`);
        } else {
            this.lobbies.set(req.lobbyName, {
                users:      [user],
                private:    req.private,
                size:       req.size,
                password:   req.password,
                lobbyName:  req.lobbyName,
            } as IActiveLobby);
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

            if (lobby.users.length === 0)
                this.lobbies.delete(req.lobbyName);
            else
                this.lobbies.set(req.lobbyName, lobby);

        } else {
            throw new Error(`${req.lobbyName} not found`);
        }
        return `Left ${req.lobbyName} successfully`;
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
        if (req.size) 
            if (req.size < 2 || req.size > 10)
                throw new Error("Lobby size should be between 2 and 10");
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