import { injectable, inject } from "inversify";
import { LobbyManagerService } from "./lobby-manager.service";
import { ArenaFfa } from "./arena-ffa";
import { ArenaSolo } from "./arena-solo";
import { ArenaCoop } from "./arena-coop";
import { IActiveLobby, IGameplayChat, IGameplayDraw, GameMode } from "../../interfaces/game";
import { IUser } from "../../interfaces/user-manager";

import Types from '../../types';
import * as io from 'socket.io';

@injectable()
export class GameManagerService {

    private arenas: Map<number, ArenaFfa | ArenaSolo | ArenaCoop>;
    private userMapArenaId: Map<string, number>;
    private arenaId: number;

    private socketServer: io.Server;
    
    public constructor(@inject(Types.LobbyManagerService) private lobServ: LobbyManagerService) {

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

    private setupArena(lobby: IActiveLobby): void {
        const room = `arena${this.arenaId}`;

        this.addUsersToArena(lobby, room, this.arenaId);

        // TODO get arena rules
        const arena = this.createArenaAccordingToMode(lobby, room);

        this.lobServ.lobbies.delete(lobby.lobbyName);
        
        this.arenas.set(this.arenaId, arena);
        this.socketServer.in(room).emit("game-start");

        arena.start();
    }

    private createArenaAccordingToMode(lobby: IActiveLobby, room: string): ArenaFfa | ArenaSolo | ArenaCoop {
        switch(lobby.mode) {
            case GameMode.FFA:
                return new ArenaFfa(lobby.users, lobby.size, room, this.socketServer);
            case GameMode.SOLO:
                return new ArenaSolo(lobby.users, lobby.size, room, this.socketServer);
            case GameMode.COOP:
                return new ArenaCoop(lobby.users, lobby.size, room, this.socketServer);
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
            if (u.socket)
            u.socket.join(room);
            this.userMapArenaId.set(u.username, arenaID);
        });
    }
    
}