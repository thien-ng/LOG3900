import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw, IDrawing, IPoints, IGameplayReady, GameMode, IGameplayEraser, IEraser, Bot, IGameplayAnnouncement } from "../../interfaces/game";
import { IGameRule } from "../../interfaces/rule";
import { GameManagerService } from "./game-manager.service";
import { MeanBot } from "./bots/meanBot";
import { KindBot } from "./bots/kindBot";
import { HumourBot } from "./bots/humourBot";

import * as io from 'socket.io';

const format = require('string-format');

const StringBuilder = require("string-builder");
const CHECK_INTERVAL_TIME = 500;
const ONE_SEC = 1000;
const ANNOUNCEMENT = "{0} has disconnected";

export abstract class Arena {

    protected gm : GameManagerService;

    protected socketServer:        io.Server;
    protected rules:               IGameRule[];
    protected room:                string;
    protected curRule:             IGameRule;
    protected userMapPoints:       Map<string, number>;
    protected type:                GameMode;
    
    protected userMapReady:        Map<string, boolean>;
    protected dcPlayer:            string[];
    protected users:               IUser[];
    protected isAllDc:             boolean;

    private arenaId:               number;
    private chronometerTimer:      number;
    private hintPtr:               number;
    
    protected curArenaInterval:    NodeJS.Timeout;
    public  chronometerInterval:   NodeJS.Timeout;

    public gameMessages:           IGameplayAnnouncement[];
    
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
        this.hintPtr        = 0;
        this.isAllDc        = false;
        this.gameMessages   = [];

        this.initReadyMap();
        this.setupPoints();
    }

    public abstract start(): void;
    public abstract receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw | IGameplayReady | IGameplayEraser): void;

    protected abstract startBotDrawing(botName: string, arenaTime: number): NodeJS.Timeout;
    protected abstract botAnnounceStart(): void;
    protected abstract botAnnounceEndSubGame(): void;
    protected abstract handleGameplayChat(mes: IGameplayChat): void;
    protected abstract updatePoints(username: string, time: number, ratio: number): void;
    protected abstract sendCurrentPointToUser(mes: IGameplayChat): void;

    protected sendAnswer(answer: string): void {
        const announcement: IGameplayAnnouncement = {
            username: "Server",
            content: `Answer was: ${answer}.`,
            isServer: true,
        };
        this.sendToChat(announcement);
    }

    protected sendHint(botName: string): void {
        const totalHint = this.curRule.clues.length;
        const announcement: IGameplayAnnouncement = {
            username: botName,
            content: `Hint: ${this.curRule.clues[this.hintPtr % totalHint]}`,
            isServer: false,
        };
        this.sendToChat(announcement);
    }

    protected isHintingRequest(mes: IGameplayChat): boolean {
        return mes.content.startsWith("!hint");
    }

    protected handleGameplayReady(mes: IGameplayReady): void {
        this.userMapReady.set(mes.username, true);
    }

    public disconnectPlayer(username: string): void {
        // disconnect user from arena but does not remove from users list to be persisted in db
        const user = this.users.find(u => {return u.username === username});
        if (user && user.socket) {
            this.dcPlayer.push(user.username);
            user.socket.leave(this.room);

            this.sendToChat({username: "Server", content: format(ANNOUNCEMENT, user.username), isServer: true});
        }

        let count = 0;
        this.users.forEach(u => {
            // count users who are not bots
            if (!this.isBot(u.username))
                count++;
        });
        if (count === 0)
            this.isAllDc = true;
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
                this.cancelGame();
            }
            numOfTries++;

        }, CHECK_INTERVAL_TIME);
    }

    protected end(): void {
        console.log("[Debug] End routine");
        const pts = this.preparePtsToBePersisted();
        console.log("[Debug] end game points are: ", pts);
        console.log("[Debug] disconnected players: ", this.dcPlayer);
        this.users.forEach(u => {
            this.socketServer.to(this.room).emit("game-over", {points: pts});
            
            if (u.socket)
                u.socket.leave(this.room);
        });

        clearInterval(this.chronometerTimer);

        this.gm.persistPoints(pts, this.chronometerTimer / ONE_SEC, this.type);
        this.gm.deleteArena(this.arenaId);
    }

    private cancelGame(): void {
        console.log("[Debug] Cancel routine");
        this.users.forEach(u => {
            this.socketServer.to(this.room).emit("game-over");
            
            if (u.socket)
                u.socket.leave(this.room);
        });
        this.gm.deleteArena(this.arenaId);
    }

    protected mapToDrawing(draw: IGameplayDraw | IGameplayEraser): IDrawing | IEraser {
        if (this.isEraser(draw))
            return {
                type: draw.type,
                x: draw.x,
                y: draw.y,
                width: draw.width,
                eraser: draw.eraser,
            };
        return {
            startPosX:  draw.startPosX,
            startPosY:  draw.startPosY,
            endPosX:    draw.endPosX,
            endPosY:    draw.endPosY,
            color:      draw.color,
            width:      draw.width,
            isEnd:      draw.isEnd,
            format:     draw.format,
            type:       draw.type,
        };
    }

    private isEraser(draw: IGameplayDraw | IGameplayEraser): draw is IGameplayEraser {
        return "eraser" in draw;
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

    protected isBot(username: string): boolean {
        return username === Bot.humour || username === Bot.kind || username === Bot.mean;
    }

    protected initBot(botName: string): MeanBot | KindBot | HumourBot {
        switch(botName) {
            case Bot.mean:
                return new MeanBot(this.socketServer, botName);
            case Bot.humour:
                return new HumourBot(this.socketServer, botName);
            case Bot.kind:
                return new KindBot(this.socketServer, botName);
            default:
                return new KindBot(this.socketServer, botName);
        }
    }

    private initReadyMap(): void {
        this.userMapReady = new Map<string, boolean>();
        this.users.forEach(u => { this.userMapReady.set(u.username, false); });
    }

    private checkIfEveryoneIsReady(): boolean {
        let isEveryoneReady = true;

        this.userMapReady.forEach((state: boolean, key: string) => {
            if (!this.dcPlayer.includes(key) && !this.isBot(key)) {
                if (state == false)
                    isEveryoneReady = false;
            }
        });

        return isEveryoneReady;
    }

    private preparePtsToBePersisted(): IPoints[] {
        const ptsList: IPoints[] = [];
        this.userMapPoints.forEach((pts: number, key: string) => {
            if (!this.isBot(key))
                ptsList.push({username: key, points: pts});
        });

        ptsList.sort((n1,n2) => {
            if (n1.points > n2.points) return -1;
            if (n1.points < n2.points) return 1;
            return 0;
        });

        return ptsList;
    }

    private startChronometer(): void {
        this.chronometerTimer = 0;
        this.chronometerInterval = setInterval(() => {
            this.chronometerTimer += ONE_SEC;
        }, ONE_SEC)
    }

    protected sendToChat(obj: IGameplayAnnouncement): void {
        this.gameMessages.push(obj);
        this.socketServer.to(this.room).emit("game-chat", obj);
    }

    protected isUserDc(username: string): boolean {
        return this.dcPlayer.includes(username);
    }

}
