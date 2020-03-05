import { injectable, inject } from "inversify";
import { LobbyManagerService } from "./lobby-manager.service";
import { Arena } from "./arena";
import { IActiveLobby, IGameplayChat, IGameplayDraw } from "../../interfaces/game";
import { GameCreatorService } from "../game/game-creator.service";

import Types from '../../types';
import * as io from 'socket.io';

@injectable()
export class GameManagerService {

    private arenas: Map<number, Arena>;
    private arenaId: number;

    private socketServer: io.Server;
    
    public constructor(
        @inject(Types.LobbyManagerService) private lobServ: LobbyManagerService,
        @inject(Types.GameCreatorService) private creatorServ: GameCreatorService) {

        // If statement to make server compile`
        if (this.creatorServ) {}
        
        this.arenas = new Map<number, Arena>();
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

    public sendMessageToArena(mes: IGameplayChat | IGameplayDraw): void {
        const arena = this.arenas.get(mes.arenaID);

        if (arena)
            arena.receiveInfo(mes);
    }

    private setupArena(lobby: IActiveLobby): void {
        const room = `arena${this.arenaId}`;

        // add users to socket room
        lobby.users.forEach(u => {
            if (u.socket)
                u.socket.join(room);
            else
                throw new Error("Missing socket to instanciate arena");
        });

        // TODO get arena rules
        const arena = new Arena(lobby.users, lobby.size, room, this.socketServer);

        this.arenas.set(this.arenaId, arena);
        this.arenaId++;
        
        this.socketServer.in(room).emit("game-start");

        this.lobServ.lobbies.delete(lobby.lobbyName);

        arena.start();
    }
    
}