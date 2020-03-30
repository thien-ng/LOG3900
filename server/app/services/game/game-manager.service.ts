import { injectable, inject } from "inversify";
import { LobbyManagerService } from "./lobby-manager.service";
import { ArenaFfa } from "./arena-ffa";
import { ArenaSolo } from "./arena-solo";
import { ArenaCoop } from "./arena-coop";
import { IActiveLobby, IGameplayChat, IGameplayDraw, GameMode, IPoints, IGameplayReady, IUserPt } from "../../interfaces/game";
import { RulesDbService } from "../../database/rules-db.service";
import { GameDbService } from "../../database/game-db.service";
import { IGameRule } from "../../interfaces/rule";
import { Time } from "../../utils/date";

import Types from '../../types';
import * as io from 'socket.io';

@injectable()
export class GameManagerService {

    private arenas: Map<number, ArenaFfa | ArenaSolo | ArenaCoop>;
    private userMapArenaId: Map<string, number>;
    private arenaId: number;

    private socketServer: io.Server;

    public constructor(
        @inject(Types.LobbyManagerService) private lobServ: LobbyManagerService,
        @inject(Types.RulesDbService) private rulesDb: RulesDbService,
        @inject(Types.GameDbService) private gameDb: GameDbService) {

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
        this.verifyAmountPlayer(lobby);

        this.setupArena(lobby);
    }

    private verifyAmountPlayer(lobby: IActiveLobby): void {
        let minAmount: number = 0, maxAmount: number = 0;
        switch (lobby.mode) {
            case GameMode.FFA:
                minAmount = 2;
                maxAmount = 9;
                break;
            case GameMode.COOP:
                minAmount = 2;
                maxAmount = 4;
                break;
            case GameMode.SOLO:
                minAmount = 1;
                maxAmount = 1;
                break;
        }
        if (lobby.users.length < minAmount || lobby.users.length > maxAmount) {
            throw new Error(`the amount of players in the lobby must be between ${minAmount} and ${maxAmount} to play in ${lobby.mode}.`);
        }
    }

    public sendMessageToArena(socket: io.Socket, mes: IGameplayChat | IGameplayDraw | IGameplayReady): void {
        const arenaId = this.userMapArenaId.get(mes.username) as number;
        const arena = this.arenas.get(arenaId);

        if (arena)
            arena.receiveInfo(socket, mes);
    }

    public deleteArena(arenaId: number): void {
        this.arenas.delete(arenaId);
    }

    private async setupArena(lobby: IActiveLobby): Promise<void> {
        const room = `arena${this.arenaId}`;

        this.addUsersToArena(lobby, room, this.arenaId);

        const rules: IGameRule[] = await this.rulesDb.getRules();

        if (rules.length < lobby.users.length)
            throw new Error("Not enough drawings are in db");

        const arena = this.createArenaAccordingToMode(this.arenaId, lobby, room, rules);

        this.lobServ.lobbies.delete(lobby.lobbyName);

        this.arenas.set(this.arenaId, arena);
        this.arenaId++;

        this.socketServer.in(room).emit("game-start");

        arena.start();
    }

    private createArenaAccordingToMode(arenaId: number, lobby: IActiveLobby, room: string, rules: IGameRule[]): ArenaFfa | ArenaSolo | ArenaCoop {
        switch (lobby.mode) {
            case GameMode.FFA:
                return new ArenaFfa(GameMode.FFA, arenaId, lobby.users, room, this.socketServer, rules, this);
            case GameMode.SOLO:
                return new ArenaSolo(GameMode.SOLO, arenaId, lobby.users, room, this.socketServer, rules, this);
            case GameMode.COOP:
                return new ArenaCoop(GameMode.COOP, arenaId, lobby.users, room, this.socketServer, rules, this);
        }
    }

    public handleDisconnect(username: string): void {
        const arenaId = this.userMapArenaId.get(username) as number;
        const arena = this.arenas.get(arenaId);

        if (arena)
            arena.disconnectPlayer(username);
    }

    public persistPoints(pts: IPoints[], timer: number, type: GameMode): void {
        const winner = this.determineWinner(pts);
        const users = this.getPlayersInGame(pts);
        const date = Time.today();
        this.gameDb.registerGame({ type: type, date: date, timer: timer, winner: winner, users: users });
    }

    private determineWinner(pts: IPoints[]): string {
        let maxPts = -1;
        let winner = "";
        pts.forEach(p => {
            if (p.points > maxPts) {
                winner = p.username;
                maxPts = p.points;
            }
        });

        return winner;
    }

    private getPlayersInGame(pts: IPoints[]): IUserPt[] {
        const list: IUserPt[] = [];
        pts.forEach(p => { list.push({ username: p.username, point: p.points }) });
        return list;
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
