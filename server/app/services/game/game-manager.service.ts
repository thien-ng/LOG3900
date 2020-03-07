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
    private userMapArenaId: Map<string, number>;
    private arenaId: number;

    private socketServer: io.Server;
    
    public constructor(
        @inject(Types.LobbyManagerService) private lobServ: LobbyManagerService,
        @inject(Types.CardsDbService) private db: CardsDbService) {

        this.arenas = new Map<number, Arena>();
        this.userMapArenaId = new Map<string, number>();
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
        const arenaId = this.userMapArenaId.get(mes.username) as number;
        const arena = this.arenas.get(arenaId);

        if (arena)
            arena.receiveInfo(mes);
    }

    private setupArena(lobby: IActiveLobby): void {
        const room = `arena${this.arenaId}`;

        this.addUsersToArena(lobby, room, this.arenaId);

        this.lobServ.lobbies.delete(lobby.lobbyName);

        // TODO get arena rules
        this.db.getRulesByGameID(lobby.gameID);
        const arena = new Arena(lobby.users, lobby.size, room, this.socketServer);

        this.arenas.set(this.arenaId, arena);
        // TODO uncomment later to increment arena id
        // this.arenaId++;

        this.socketServer.in(room).emit("game-start");

        arena.start();
    }

    private addUsersToArena(lobby: IActiveLobby, room: string, arenaID: number): void {        
        // add users to socket room
        lobby.users.forEach(u => {
            if (u.socket)
            u.socket.join(room);
            this.userMapArenaId.set(u.username, arenaID);
        });
    }
    
}