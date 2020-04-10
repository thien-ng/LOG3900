import { Arena } from "./arena";
import { IGameplayChat, IGameplayDraw, IGameplayReady, GameMode, EventType, IDrawing, Bot } from "../../interfaces/game";
import { Difficulty } from "../../interfaces/creator";
import { IUser } from "../../interfaces/user-manager";
import { IGameRule } from "../../interfaces/rule";
import { GameManagerService } from "./game-manager.service";
import { DrawingTools } from "./utils/drawing-tools";
import * as bot from "./bots/bot";

import * as io from 'socket.io';
import { clearInterval } from "timers";

const format = require('string-format');

const ANNOUNCEMENT = "{0} has found the answer";
const ONE_SEC = 1000;

export class ArenaSprint extends Arena {

    private timeRemaining: number;
    private guessLeft: number;
    private wordGuessedRight: number;
    private guessPerImage: number;
    private timePerImage: number;
    private pointsMult: number;

    private rulePtr: number;
    private drawerBot: bot.Bot;

    public constructor(type: GameMode, arenaId: number, users: IUser[], room: string, io: io.Server, rules: IGameRule[], gm: GameManagerService) {
        super(type, arenaId, users, room, io, rules, gm)

        this.setDifficulty(rules[0].difficulty);
        this.wordGuessedRight = 0;
        this.rulePtr = 0;
    }

    private assignRule(): void {
        this.curRule = this.rules[this.rulePtr++];
        if (this.rulePtr >= this.rules.length) {
            this.rulePtr = 0;
        }
    }

    public start(): void {
        console.log("[Debug] Starting arena SOLO", this.room);
        this.chooseBot();

        try {
            this.checkArenaLoadingState(() => {
                this.botAnnounceStart();
                this.startSubGame();
            });
        } catch (e) {
            this.end();
        }
    }

    public startSubGame(): void {

        this.initSubGame();
        this.curArenaInterval = setInterval(() => {
            console.log("[Debug] time remaining: ", this.timeRemaining);

            this.socketServer.to(this.room).emit("game-timer", { time: this.timeRemaining / ONE_SEC });

            if (this.timeRemaining <= 0) {
                clearInterval(this.curArenaInterval);
                this.sendAnswer(this.curRule.solution);
                this.end();
            }
            this.timeRemaining -= ONE_SEC;

        }, ONE_SEC);
    }

    public receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw | IGameplayReady): void {
        switch (mes.event) {
            case EventType.chat:
                this.handleGameplayChat(mes as IGameplayChat);
                break;
            case EventType.ready:
                this.handleGameplayReady(mes as IGameplayReady);
                break;
        }
    }

    protected handleGameplayChat(mes: IGameplayChat): void {
        this.sendToChat({ username: mes.username, content: mes.content, isServer: false });

        if (this.isHintingRequest(mes)) {
            this.sendHint(this.drawerBot.username);
            return;
        }

        if (this.isRightAnswer(mes.content)) {
            // clearInterval(this.drawerBot.interval);

            //add time,
            this.timeRemaining += this.timePerImage;

            //anounce
            this.sendToChat({username: "Server", content: format(ANNOUNCEMENT, mes.username), isServer: true });

            //add to score
            this.wordGuessedRight++;
            this.botAnnounceEndSubGame();
            this.handlePoints();
            this.sendCurrentPointToUser(mes);
            this.resetSubGame();

        } else {
            this.guessLeft--;
            this.socketServer.to(this.room).emit("game-guessLeft", { guessLeft: this.guessLeft });
        }
    }

    private setDifficulty(diff: Difficulty): void {
        switch (diff) {
            case Difficulty.HARD:
                this.guessPerImage = 2;
                this.timePerImage = 15 * ONE_SEC;
                this.timeRemaining = 20 * ONE_SEC;
                this.pointsMult = 14;
                break;
            case Difficulty.MEDIUM:
                this.guessPerImage = 3;
                this.timePerImage = 10 * ONE_SEC;
                this.timeRemaining = 30 * ONE_SEC;
                this.pointsMult = 10;
                break;
            case Difficulty.EASY:
            default:
                this.guessPerImage = 4;
                this.timePerImage = 8 * ONE_SEC;
                this.timeRemaining = 40 * ONE_SEC;
                this.pointsMult = 6;
                break;
        }
    }

    private initSubGame(): void {
        //show an image or build one.
        this.startBotDrawing(this.drawerBot.username, this.timePerImage);
        
        this.assignRule();

        //reset guess left
        this.guessLeft = this.guessPerImage;
        this.socketServer.to(this.room).emit("game-guessLeft", { guessLeft: this.guessLeft });
    }

    private resetSubGame(): void {
        this.assignRule();
        this.startBotDrawing(this.drawerBot.username, this.timePerImage);

        //reset guess left
        this.guessLeft = this.guessPerImage;
        this.socketServer.to(this.room).emit("game-guessLeft", { guessLeft: this.guessLeft });
    }

    protected handlePoints(): void {
        this.users.forEach(u => {
            if (!this.isUserDc(u.username)) // give points to all connected users
                this.userMapPoints.set(u.username, this.wordGuessedRight * this.pointsMult)
        });
    }

    protected updatePoints(username: string, time: number, ratio: number): void {
        // not implemented function from parent
    }

    protected sendCurrentPointToUser(mes: IGameplayChat): void {
        const user = this.users.find(u => {return u.username === mes.username}) as IUser;
        const pts = this.userMapPoints.get(user.username) as number;
        this.socketServer.to(this.room).emit("game-points", {point: pts});
    }

    protected startBotDrawing(botName: string, arenaTime: number): void {
        const drawings: IDrawing[] = DrawingTools.prepareGameRule(this.curRule.drawing);
        this.drawerBot.draw(this.room, arenaTime, drawings, this.curRule.displayMode, this.curRule.side);
    }

    protected botAnnounceStart(): void {
        this.drawerBot.launchTauntStart(this.room, this.gameMessages);
    }

    protected botAnnounceEndSubGame(): void {
        this.drawerBot.launchTaunt(this.room, this.gameMessages);
    }
    
    private chooseBot(): void {
        const rand = Math.floor(Math.random() * Object.keys(Bot).length);
        const botName = Bot[Object.keys(Bot)[rand]];
        this.drawerBot = this.initBot(botName);
    }
}
