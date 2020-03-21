import { injectable, inject } from "inversify";
import { LobbyManagerService } from "./lobby-manager.service";
import { ArenaFfa } from "./arena-ffa";
import { ArenaSolo } from "./arena-solo";
import { ArenaCoop } from "./arena-coop";
import { IActiveLobby, IGameplayChat, IGameplayDraw, GameMode, IPoints } from "../../interfaces/game";
import { RulesDbService } from "../../database/rules-db.service";
import { IGameRule } from "../../interfaces/rule";

import Types from '../../types';
import * as io from 'socket.io';

@injectable()
export class GameManagerService {

    private arenas: Map<number, ArenaFfa | ArenaSolo | ArenaCoop>;
    private userMapArenaId: Map<string, number>;
    private arenaId: number;

    private socketServer: io.Server;
    
    public constructor(
        @inject(Types.LobbyManagerService)  private lobServ: LobbyManagerService,
        @inject(Types.RulesDbService)       private db: RulesDbService) {

        this.arenas = new Map<number, ArenaFfa | ArenaSolo | ArenaCoop>();
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

    public sendMessageToArena(socket: io.Socket, mes: IGameplayChat | IGameplayDraw): void {
        const arenaId = this.userMapArenaId.get(mes.username) as number;
        const arena = this.arenas.get(arenaId);

        if (arena)
            arena.receiveInfo(socket, mes);
    }

    public deleteArena(arenaId: number): void {
        this.arenas.delete(arenaId);
    }

    public persistPoints(pts: IPoints[]): void {
        // TODO persist to db points
    }

    private async setupArena(lobby: IActiveLobby): Promise<void> {
        const room = `arena${this.arenaId}`;

        this.addUsersToArena(lobby, room, this.arenaId);

        const rules: IGameRule[] = await this.db.getRules();
        const arena = this.createArenaAccordingToMode(this.arenaId, lobby, room, rules);

        this.lobServ.lobbies.delete(lobby.lobbyName);
        
        this.arenas.set(this.arenaId, arena);
        this.socketServer.in(room).emit("game-start");

        arena.start();
    }

    private createArenaAccordingToMode(arenaId: number, lobby: IActiveLobby, room: string, rules: IGameRule[]): ArenaFfa | ArenaSolo | ArenaCoop {
        switch(lobby.mode) {
            case GameMode.FFA:
                return new ArenaFfa(arenaId, lobby.users, room, this.socketServer, rules, this);
            case GameMode.SOLO:
                return new ArenaSolo(arenaId, lobby.users, room, this.socketServer, rules, this);
            case GameMode.COOP:
                return new ArenaCoop(arenaId, lobby.users, room, this.socketServer, rules, this);
        }
    }

    public handleDisconnect(username: string): void {
        const arenaId = this.userMapArenaId.get(username) as number;
        const arena = this.arenas.get(arenaId);

        if (arena)
            arena.disconnectPlayer(username);
    }

    private addUsersToArena(lobby: IActiveLobby, room: string, arenaID: number): void {        
        // add users to socket room
        lobby.users.forEach(u => {
            if (u.socket) {
                u.socket.join(room);
                this.userMapArenaId.set(u.username, arenaID);
            }
        });
    }
    
}