import { Arena } from "./arena";
import { IGameplayChat, IGameplayDraw, IGameplayReady, GameMode, EventType, IDrawing } from "../../interfaces/game";

import * as io from 'socket.io';
import { Difficulty } from "../../interfaces/creator";
import { IUser } from "../../interfaces/user-manager";
import { IGameRule } from "../../interfaces/rule";
import { GameManagerService } from "./game-manager.service";
import { DrawingTools } from "./utils/drawing-tools";
import { Bot } from "./bots/bot";

const ANNOUNCEMENT = "{0} has found the answer";
const ONE_SEC = 1000;
const format = require('string-format');

export class ArenaSolo extends Arena {

    private timeRemaining: number;
    private guessLeft: number;
    private wordGuessedRight: number;
    private guessPerImage: number;
    private timePerImage: number;
    private pointsMult: number;

    private rulePtr: number;
    private drawing: NodeJS.Timeout;
    private drawer_bot: Bot;

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
        this.drawer_bot = this.initBot("drawing-bot");

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

        this.resetSubGame();
        this.curArenaInterval = setInterval(() => {
            console.log("[Debug] time remaining: ", this.timeRemaining);

            this.socketServer.to(this.room).emit("game-timer", { time: this.timeRemaining / ONE_SEC });

            if (this.timeRemaining <= 0) {
                clearInterval(this.curArenaInterval);
                this.handlePoints();
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
            case EventType.hint:
                this.handleGameplayHint();
                break;
        }
    }

    protected handleGameplayChat(mes: IGameplayChat): void {
        this.sendToChat({ username: mes.username, content: mes.content, isServer: false });
        if (this.isRightAnswer(mes.content)) {
            //add time,
            this.timeRemaining += this.timePerImage;

            //anounce
            this.sendToChat({
                username: "Server",
                content: format(ANNOUNCEMENT, mes.username),
                isServer: true
            });
            //add to score
            this.wordGuessedRight++;
            this.botAnnounceEndSubGane();

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
                this.timePerImage = 3 * ONE_SEC;
                this.timeRemaining = 20 * ONE_SEC;
                this.pointsMult = 1.4;
                break;
            case Difficulty.MEDIUM:
                this.guessPerImage = 3;
                this.timePerImage = 7 * ONE_SEC;
                this.timeRemaining = 30 * ONE_SEC;
                this.pointsMult = 1;
                break;
            case Difficulty.EASY:
            default:
                this.guessPerImage = 4;
                this.timePerImage = 10 * ONE_SEC;
                this.timeRemaining = 40 * ONE_SEC;
                this.pointsMult = 0.6;
                break;
        }
    }

    private resetSubGame(): void {
        //show an image or build one.
        clearInterval(this.drawing);
        this.drawing = this.startBotDrawing(this.drawer_bot.username, this.timePerImage);

        //reset guess left
        this.guessLeft = this.guessPerImage;
        this.socketServer.to(this.room).emit("game-guessLeft", { guessLeft: this.guessLeft });

    }
    protected handlePoints(): void {
        this.userMapPoints.set(this.users[0].username, this.wordGuessedRight * this.pointsMult);
    }

    protected startBotDrawing(botName: string, arenaTime: number): NodeJS.Timeout {
        this.assignRule();
        const drawings: IDrawing[] = DrawingTools.prepareGameRule(this.curRule.drawing);
        return this.drawer_bot.draw(this.room, arenaTime, drawings, this.curRule.displayMode, this.curRule.side);
    }

    protected botAnnounceStart(): void {
        this.drawer_bot.launchTauntStart(this.room);
    }

    protected botAnnounceEndSubGane(): void {
        this.drawer_bot.launchTaunt(this.room);
    }
}
