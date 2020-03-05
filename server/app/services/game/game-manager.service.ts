import { injectable, inject } from "inversify";
import { LobbyManagerService } from "./lobby-manager.service";
import { Arena } from "./arena";
import { IActiveLobby, IGameplayChat, IGameplayDraw } from "../../interfaces/game";
import { CardsDbService } from "../../database/cards-db.service";

import Types from '../../types';
import * as io from 'socket.io';

@injectable()
export class GameManagerService {

    private arenas: Map<number, Arena>;
    private arenaId: number;

    private socketServer: io.Server;
    
    public constructor(
        @inject(Types.LobbyManagerService) private lobServ: LobbyManagerService,
        @inject(Types.CardsDbService) private db: CardsDbService) {

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

        this.addUsersInRoom(lobby, room);

        this.lobServ.lobbies.delete(lobby.lobbyName);

        // TODO get arena rules
        this.db.getRulesByGameID(lobby.gameID);
        const arena = new Arena(lobby.users, lobby.size, room, this.socketServer);

        this.arenas.set(this.arenaId, arena);
        this.arenaId++;
    
        this.socketServer.in(room).emit("game-start");

        arena.start();
    }

    private addUsersInRoom(lobby: IActiveLobby, room: string): void {
        lobby.users.forEach(u => {
            if (!u.socket)
            throw new Error("Missing socket to instanciate arena");
        });
        
        // add users to socket room
        lobby.users.forEach(u => {
            if (u.socket)
                u.socket.join(room);
        });
    }
    
}