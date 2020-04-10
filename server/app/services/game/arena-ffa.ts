import { Arena } from "./arena";
import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw, IGameplayReady, GameMode, IGameplayEraser, IDrawing, EventType } from "../../interfaces/game";
import { IGameRule } from "../../interfaces/rule";
import { GameManagerService } from "./game-manager.service";
import { DrawingTools } from "./utils/drawing-tools";
import { MeanBot } from "./bots/meanBot";
import { KindBot } from "./bots/kindBot";
import { HumourBot } from "./bots/humourBot";
import { Bot } from "./bots/bot";

import * as io from 'socket.io';
import { Difficulty } from "../../interfaces/creator";

const format = require('string-format');

const ANNOUNCEMENT = "{0} has found the answer";
const ONE_SEC = 1000;
const TOTAL_TIME = 30000;
const INIT_DRAW_PTS = 20;

export class ArenaFfa extends Arena {

    private drawPtr: number;

    private curTime: number;
    private userWithCorrectAns: number;

    private isEveryoneHasRightAnswer: boolean;

    private isBotDrawing: boolean;

    private botMap: Map<string, MeanBot | KindBot | HumourBot>;

    public constructor(type: GameMode, arenaId: number, users: IUser[], room: string, io: io.Server | undefined, rules: IGameRule[], gm: GameManagerService) {
        super(type, arenaId, users, room, io as io.Server, rules, gm)

        this.drawPtr = 0;
        this.userWithCorrectAns = 0;
        this.isEveryoneHasRightAnswer = false;
        this.isBotDrawing = false;
        this.botMap = new Map<string, MeanBot | KindBot | HumourBot>();
    }

    public start(): void {
        console.log("[Debug] Starting arena FFA", this.room);

        this.initBots();

        try {
            this.checkArenaLoadingState(() => {
                this.botAnnounceStart();
                this.startSubGame()
            });
        }  catch(e) {
            this.end();
        }
    }

    private startSubGame(): void {

        if (this.resetSubGame()) {
            this.end();
            return;
        }

        if (this.isBotDrawing)
            this.startBotDrawing(this.users[this.drawPtr - 1].username, TOTAL_TIME);

        let timer = 0;
        this.curArenaInterval = setInterval(() => {
            console.log("[Debug] Timer is: ", timer);

            this.socketServer.to(this.room).emit("game-timer", {time: (TOTAL_TIME - timer)/ONE_SEC});
            this.curTime = timer;

            if (timer >= TOTAL_TIME || this.isEveryoneHasRightAnswer || this.isAllDc) {
                clearInterval(this.curArenaInterval);

                if (this.isEveryoneHasRightAnswer == false) {
                    // Announce answer
                    this.sendAnswer(this.curRule.solution);
                }

                if (this.isBotDrawing) {
                    const bot = this.botMap.get(this.users[this.drawPtr - 1].username) as MeanBot | KindBot | HumourBot;
                    clearInterval(bot.interval);
                }

                this.botAnnounceEndSubGame();

                if (this.drawPtr >= this.users.length) {
                    // Handle end of game
                    this.end();
                } else {
                    // Make next person to draw
                    this.startSubGame();
                }
            }
            timer += ONE_SEC;

        }, ONE_SEC);
    }

    private resetSubGame(): void | boolean {
        this.userWithCorrectAns = 0;
        this.isEveryoneHasRightAnswer = false;
        this.isBotDrawing = false;

        this.chooseRandomRule();
        if (this.attributeRoles())
            return true;
    }

    public receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw | IGameplayReady | IGameplayEraser): void {
        switch(mes.event) {
            case EventType.draw:
                this.handleGameplayDraw(socket, mes as IGameplayDraw | IGameplayEraser);
                break;
            case EventType.chat:
                this.handleGameplayChat(mes as IGameplayChat);
                break;
            case EventType.ready:
                this.handleGameplayReady(mes as IGameplayReady);
                break;
        }
    }

    private handleGameplayDraw(socket: io.Socket, mes: IGameplayDraw | IGameplayEraser): void {
        this.users.forEach(u => {
            if (u.username != mes.username)
                socket.to(this.room).emit("draw", this.mapToDrawing(mes))
        });
    }

    protected handleGameplayChat(mes: IGameplayChat): void {
        const answer = mes.content.toLocaleLowerCase().trim();
        const drawerUsername = this.users[this.drawPtr - 1].username;

        if (this.isHintingRequest(mes) && this.isBot(drawerUsername)) {
            this.sendToChat({username: mes.username, content: mes.content, isServer: false});
            this.sendHint(drawerUsername);
            return;
        }

        if (this.isRightAnswer(answer) && this.isNotCurrentDrawer(mes.username)) {
            const encAnswer = this.encryptAnswer(mes.content);
            
            this.sendToChat({username: mes.username, content: encAnswer, isServer: false});
            this.sendToChat({username: "Server", content: format(ANNOUNCEMENT, mes.username), isServer: true});
            
            const time = (TOTAL_TIME - this.curTime)/ONE_SEC;
            const ratio = 1 - this.calculateRatio()

            this.userWithCorrectAns++;

            this.updatePoints(mes.username, time, ratio);
            this.sendCurrentPointToUser(mes);

            if (this.checkIfEveryoneHasRightAnswer())
                this.isEveryoneHasRightAnswer = true;
        } else {
            this.sendToChat({username: mes.username, content: mes.content, isServer: false});
        }
    }

    protected updatePoints(username: string, time: number, ratio: number): void {
        // Give points to guesser
        // pts = time_left * (1 - ratio_found)
        const pts = this.userMapPoints.get(username) as number;
        const newPts = Math.floor(time * ratio);
        this.userMapPoints.set(username, pts + newPts);

        // Give points to drawer
        // pts = 20 * ratio_found
        const drawName = this.users[this.drawPtr - 1].username;
        const drawPts = this.userMapPoints.get(drawName) as number;
        const drawNewPts = Math.floor(INIT_DRAW_PTS * this.calculateRatio());
        this.userMapPoints.set(drawName, drawNewPts + drawPts);
    }
    
    protected sendCurrentPointToUser(mes: IGameplayChat): void {
        const user = this.users.find(u => {return u.username === mes.username}) as IUser;
        const pts = this.userMapPoints.get(user.username) as number;
        this.socketServer.to(user.socketId).emit("game-points", {point: pts});

        const drawUser = this.users[this.drawPtr - 1];
        const drawPts = this.userMapPoints.get(drawUser.username) as number;
        this.socketServer.to(drawUser.socketId).emit("game-points", {point: drawPts});
    }

    protected startBotDrawing(botName: string, arenaTime: number): void {
        const drawings: IDrawing[] = DrawingTools.prepareGameRule(this.curRule.drawing);
        const bot = this.botMap.get(botName) as Bot;
        const speed = this.chooseDrawingSpeed(arenaTime);
        bot.draw(this.room, speed, drawings, this.curRule.displayMode, this.curRule.side);
    }

    protected botAnnounceStart(): void {
        this.botMap.forEach((bot: Bot, key: string) => {
            bot.launchTauntStart(this.room, this.gameMessages);
        });
    }

    protected botAnnounceEndSubGame(): void {
        this.botMap.forEach((bot: Bot, key: string) => {
            bot.launchTaunt(this.room, this.gameMessages);
        });
    }

    private chooseDrawingSpeed(arenaTime: number): number {
        switch(this.curRule.difficulty){
            case Difficulty.EASY:
                return Math.floor(arenaTime/4);
            case Difficulty.MEDIUM:
                return Math.floor(arenaTime/2);
            case Difficulty.HARD:
                return arenaTime;
            default:
                return arenaTime;
        }
    }

    private attributeRoles(): void | boolean {
        let user = this.users[this.drawPtr++];

        while (this.isUserDc(user.username)) {

            // if player is disconnect, increment drawer pointer
            if (this.drawPtr++ >= this.users.length) {
                clearInterval(this.curArenaInterval);
                return true
            }
            user = this.users[this.drawPtr];
        }
        this.updateDrawerRole(user);
    }

    private updateDrawerRole(drawer: IUser): void {
        this.users.forEach(u => {
            if (!this.isUserDc(u.username) && u.username !== drawer.username)
                this.socketServer.to(u.socketId).emit("game-drawer", {username: drawer.username});
        });

        if (this.isBot(drawer.username))
            this.isBotDrawing = true;
        else
            this.socketServer.to(drawer.socketId).emit("game-drawer", {username: drawer.username, object: this.curRule.solution});
    }

    private calculateRatio(): number {
        return Math.floor(this.userWithCorrectAns / this.users.length);
    }

    private checkIfEveryoneHasRightAnswer(): boolean {
        return (this.users.length - this.dcPlayer.length -1 ) <= this.userWithCorrectAns; //-1 to ignore drawer
    }

    private initBots(): void {
        this.users.forEach(u => {
            if (this.isBot(u.username)) {
                const bot = this.initBot(u.username);
                this.botMap.set(u.username, bot);
            }
        });
    }

    private isNotCurrentDrawer(username: string): boolean {        
        return this.users[this.drawPtr - 1].username !== username;
    }

}
