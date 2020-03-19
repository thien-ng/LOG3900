import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw, IDrawing, IPoints } from "../../interfaces/game";
import { IGameRule } from "../../interfaces/rule";
import { GameManagerService } from "./game-manager.service";

import * as io from 'socket.io';

const StringBuilder = require("string-builder");

export abstract class Arena {

    protected gm : GameManagerService;

    protected socketServer:  io.Server;
    protected users:         IUser[];
    protected rules:         IGameRule[];
    protected room:          string;
    protected dcPlayer:      string[];
    protected curRule:       IGameRule;
    protected userMapPoints: Map<string, number>;

    private arenaId: number;

    public constructor(arenaId: number, users: IUser[], room: string, io: io.Server, rules: IGameRule[], gm: GameManagerService) {
        this.users          = users;
        this.room           = room;
        this.socketServer   = io;
        this.rules          = rules;
        this.dcPlayer       = [];
        this.curRule        = this.rules[0];
        this.gm             = gm;
        this.arenaId        = arenaId;

        this.setupPoints();
    }
    
    public abstract start(): void;
    public abstract receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw): void;

    protected abstract handleGameplayChat(mes: IGameplayChat): void;
    protected abstract handlePoints(): void;

    public disconnectPlayer(username: string): void {
        // disconnect user from arena but does not remove from users list to be persisted in db
        const user = this.users.find(u => {return u.username === username});
        if (user && user.socket) {
            this.dcPlayer.push(user.username);
            user.socket.leave(this.room);
        }
    }

    protected end(): void {
        console.log("[Debug] End routine");
        this.users.forEach(u => {
            this.socketServer.to(this.room).emit("game-over");
        });
        
        const pts = this.preparePtsToBePersisted();
        this.gm.persistPoints(pts);
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
        }
    }

    protected isDraw(mes: IGameplayChat | IGameplayDraw): mes is IGameplayDraw {
        return "startPosX" in mes;
    }

    protected chooseRandomRule(): void {
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
        return enc;
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

    private preparePtsToBePersisted(): IPoints[] {
        const ptsList: IPoints[] = [];
        this.userMapPoints.forEach((pts: number, key: string) => {
            ptsList.push({username: key, points: pts});
        });
        return ptsList;
    }

}