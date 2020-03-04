import { injectable, inject } from "inversify";
import { LobbyManagerService } from "./lobby-manager.service";
import { Arena } from "./arena";
import { IActiveLobby } from "../../interfaces/game";
import { GameCreatorService } from "../game/game-creator.service";

import Types from '../../types';
import * as io from 'socket.io';

@injectable()
export class GameManagerService {

    private arenas: Arena[];
    private arenaId: number;

    private socketServer: io.Server;
    
    public constructor(
        @inject(Types.LobbyManagerService) private lobServ: LobbyManagerService,
        @inject(Types.GameCreatorService) private creatorServ: GameCreatorService) {

        // If statement to make server compile`
        if (this.creatorServ) {}
        
        this.arenas = [];
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

        // add users to socket room
        lobby.users.forEach(u => {
            if (u.socket)
                u.socket.join(room);
            else
                throw new Error("Missing socket to instanciate arena");
        });

        // TODO get arena rules
        const arena = new Arena(lobby.users, lobby.size, room, this.socketServer);

        this.arenas.push(arena);
        
        this.socketServer.in(room).emit("start-game");

        this.lobServ.lobbies.delete(lobby.lobbyName);
    }
    
}