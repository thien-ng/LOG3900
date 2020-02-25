import { injectable, inject } from "inversify";
import { IJoinLobby, ILobby } from "../../interfaces/game";
import { IUser } from "../../interfaces/user-manager";
import { UserManagerService } from "../user-manager.service";

import * as io from 'socket.io';
import Types from '../../types';
import { IStatus } from "../../interfaces/communication";

@injectable()
export class LobbyManagerService {

    private lobbies: Map<string , ILobby>
    private socket:  io.Server;

    public constructor(@inject(Types.UserManagerService) private userServ: UserManagerService) {
        this.lobbies = new Map<string, ILobby>();
    }

    public initSocketServer(socket: io.Server): void {
        this.socket = socket;
    }

    public join(req: IJoinLobby): IStatus {

        const user = this.userServ.getUsersByName(req.username);
        
        if (!user)  return {status: 400, message: `${req.username} is not found in logged users`};

        const lobby = this.lobbyDoesExists(req.lobbyName);

        if (lobby) {
            if (lobby.private && this.verifyPassword(req.password, lobby.password as string)) {
                lobby.users.push(user);
            } else if (lobby.private == false) {
                lobby.users.push(user);
            } else {
                return {status: 400, message: `wrong password for lobby ${req.lobbyName}`};
            }            
        } else {
            
            this.lobbies.set(req.lobbyName, {
                users: [user],
                private: req.private,
                password: req.password
            });

        }
    }

    private verifyPassword(pw: string, lobbyPw: string): boolean {
        return pw === lobbyPw;
    }

    private lobbyDoesExists(lobbyName: string): ILobby | undefined {
        return this.lobbies.get(lobbyName);
    }


}