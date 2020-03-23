import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw, IDrawing, IPoints, IGameplayReady, GameMode } from "../../interfaces/game";
import { IGameRule } from "../../interfaces/rule";
import { GameManagerService } from "./game-manager.service";

import * as io from 'socket.io';

const StringBuilder = require("string-builder");
const CHECK_INTERVAL_TIME = 500;
const ONE_SEC = 1000;

export abstract class Arena {

    protected gm : GameManagerService;

    protected socketServer:  io.Server;
    protected users:         IUser[];
    protected rules:         IGameRule[];
    protected room:          string;
    protected dcPlayer:      string[];
    protected curRule:       IGameRule;
    protected userMapPoints: Map<string, number>;

    protected userMapReady:  Map<string, boolean>;
    protected type:          GameMode;

    private arenaId:             number;
    public  chronometerInterval: NodeJS.Timeout;
    private chronometerTimer:    number;

    public constructor(type: GameMode, arenaId: number, users: IUser[], room: string, io: io.Server, rules: IGameRule[], gm: GameManagerService) {
        this.users          = users;
        this.room           = room;
        this.socketServer   = io;
        this.rules          = rules;
        this.dcPlayer       = [];
        this.curRule        = this.rules[0];
        this.gm             = gm;
        this.arenaId        = arenaId;
        this.type           = type;

        this.initReadyMap();
        this.setupPoints();
    }
    
    public abstract start(): void;
    public abstract receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw | IGameplayReady): void;

    protected abstract handleGameplayChat(mes: IGameplayChat): void;
    protected abstract handlePoints(): void;
    protected handleGameplayReady(mes: IGameplayReady): void {
        this.userMapReady.set(mes.username, true);
    }

    public disconnectPlayer(username: string): void {
        // disconnect user from arena but does not remove from users list to be persisted in db
        const user = this.users.find(u => {return u.username === username});
        if (user && user.socket) {
            this.dcPlayer.push(user.username);
            user.socket.leave(this.room);
        }
    }

    protected checkArenaLoadingState(callback: () => void): void {
        let numOfTries = 0;
        const checkInterval = setInterval(() => {
            
            if (this.checkIfEveryoneIsReady()) {
                clearInterval(checkInterval);
                this.startChronometer();
                callback();
            }
            else if (numOfTries >= 3) {
                clearInterval(checkInterval);
                this.end(); // TODO should handle game which doesn't affect player's kdr
            }
            numOfTries++;

        }, CHECK_INTERVAL_TIME);
    }

    protected end(): void {
        console.log("[Debug] End routine");
        const pts = this.preparePtsToBePersisted();
        console.log("[Debug] end game points are: ", pts);
        

        this.users.forEach(u => {
            this.socketServer.to(this.room).emit("game-over", {points: pts});
        });

        clearInterval(this.chronometerTimer);
        
        this.gm.persistPoints(pts, this.chronometerTimer / ONE_SEC, this.type);
        this.gm.deleteArena(this.arenaId);
    }

    protected mapToDrawing(draw: IGameplayDraw): IDrawing {
        return {
            startPosX:  draw.startPosX,
            startPosY:  draw.startPosY,
            endPosX:    draw.endPosX,
            endPosY:    draw.endPosY,
            color:      draw.color,
            width:      draw.width,
            idEnd:      draw.idEnd,
        }
    }

    protected isDraw(mes: IGameplayChat | IGameplayDraw | IGameplayReady): mes is IGameplayDraw {
        return "startPosX" in mes;
    }

    protected isChat(mes: IGameplayChat | IGameplayReady): mes is IGameplayChat {
        return "content" in mes;
    }

    protected chooseRandomRule(): void {
        // May fail if there is no rules in mongodb
        const index =  Math.floor(Math.random() * this.rules.length);
        this.curRule = this.rules[index];

        // remove rules that are used already
        const newList = []
        for (let i = 0; i < this.rules.length; i++) {
            if (index != i)
                newList.push(this.rules[i]);
        }
        this.rules = newList;
    }

    protected encryptAnswer(ans: string): string {
        let enc = new StringBuilder();
        for (let i = 0; i < ans.length; i++) {
            enc.append("*");
        }
        return enc.toString();
    }

    protected isRightAnswer(ans: string): boolean {
        return this.curRule.solution === ans;
    }

    protected setupPoints(): void {
        this.userMapPoints  = new Map<string, number>();
        this.users.forEach(u => {
            this.userMapPoints.set(u.username, 0);
        });
    }

    private initReadyMap(): void {
        this.userMapReady = new Map<string, boolean>();
        this.users.forEach(u => { this.userMapReady.set(u.username, false); });
    }

    private checkIfEveryoneIsReady(): boolean {
        let isEveryoneReady = true;

        this.userMapReady.forEach((state: boolean, key: string) => {
            if (!this.dcPlayer.includes(key)) {
                if (state == false)
                    isEveryoneReady = false;
            }
        });
        
        return isEveryoneReady;
    }

    private preparePtsToBePersisted(): IPoints[] {
        const ptsList: IPoints[] = [];
        this.userMapPoints.forEach((pts: number, key: string) => {
            ptsList.push({username: key, points: pts});
        });
        return ptsList;
    }

    private startChronometer(): void {
        this.chronometerTimer = 0;
        this.chronometerInterval = setInterval(() => {
            this.chronometerTimer += ONE_SEC;
        }, ONE_SEC)
    }

}