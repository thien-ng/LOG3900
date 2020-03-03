import { injectable, inject } from "inversify";
import { LobbyManagerService } from "./lobby-manager.service";
import { Arena } from "./arena";
import { IActiveLobby } from "../../interfaces/game";

import Types from '../../types';
import * as io from 'socket.io';

@injectable()
export class GameManagerService {

    private arenas: Arena[];
    private arenaId: number;

    private socketServer: io.Server;
    
    public constructor(@inject(Types.LobbyManagerService) private lobServ: LobbyManagerService) {
        // TODO if statement to make server compile
        if (this.socketServer) {}
        this.arenaId = 0;
    }
    
    public initSocketServer(socketServer: io.Server): void {
        this.socketServer = socketServer;     
    }

    public startGame(lobbyName: string): void {
        const lobby = this.lobServ.lobbies.get(lobbyName);

        if (!lobby)
            throw new Error(`${lobbyName} is not found in active lobbies`);

        this.setupArena(lobby);
    }

    private setupArena(lobby: IActiveLobby): void {
        const room = `arena${this.arenaId++}`;

        // TODO get arena rules
        const arena = new Arena(lobby.users, lobby.size, room);

        this.arenas.push(arena);
        
        // add users to socket room
        lobby.users.forEach(u => {u.socket?.join(room)});
    }
    
}