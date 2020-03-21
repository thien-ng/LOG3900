import { Arena } from "./arena";
import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw, IGameplayAnnouncement, ICorrAns } from "../../interfaces/game";
import { IGameRule } from "../../interfaces/rule";
import { GameManagerService } from "./game-manager.service";

import * as io from 'socket.io';

const format = require('string-format');

const ANNOUNCEMENT = "{0} has found the answer";
const ONE_SEC = 1000;
const TOTAL_TIME = 30000;
const INIT_DRAW_PTS = 20;

export class ArenaFfa extends Arena {

    private drawPtr: number;
    private curArenaInterval: NodeJS.Timeout;

    private curTime: number;
    private userWithCorrectAns: ICorrAns[];

    public constructor(arenaId: number, users: IUser[], room: string, io: io.Server, rules: IGameRule[], gm: GameManagerService) {
        super(arenaId, users, room, io, rules, gm)
        
        this.drawPtr = 0;
        this.userWithCorrectAns = [];
    }

    public start(): void {
        console.log("[Debug] Starting arena", this.room);   
        try {
            this.startSubGame();
        }  catch(e) {
            this.end();
        }
    }

    private startSubGame(): void {

        this.resetSubGame();

        let timer = 0;
        this.curArenaInterval = setInterval(() => {

            console.log("[Debug] Timer is: ", timer += ONE_SEC);
            
            this.socketServer.to(this.room).emit("timer", timer/ONE_SEC);
            this.curTime = timer;

            if (timer >= TOTAL_TIME) {
                clearInterval(this.curArenaInterval);

                this.handlePoints()

                if (this.drawPtr >= this.users.length) {
                    // Handle end of game
                    this.end()
                } else {
                    // Make next person to draw
                    this.startSubGame();
                }
            }

        }, ONE_SEC);
    }

    private resetSubGame(): void {
        this.attributeRoles();
        this.chooseRandomRule();
        this.userWithCorrectAns = [];
    }

    public receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw): void {
        if (this.isDraw(mes)) {
            this.handleGameplayDraw(socket, mes);
        } else {
            this.handleGameplayChat(mes);
        }
    }

    private handleGameplayDraw(socket: io.Socket, mes: IGameplayDraw): void {
        this.users.forEach(u => {
            if (u.username != mes.username)
                socket.to(this.room).emit("draw", this.mapToDrawing(mes))
        });
    }

    protected handleGameplayChat(mes: IGameplayChat): void {
        
        if (this.isRightAnswer(mes.content)) {
            this.userWithCorrectAns.push({
                username: mes.username,
                time: TOTAL_TIME - this.curTime,
                ratio: 1 - this.calculateRatio()});

            const encAnswer = this.encryptAnswer(mes.content);
            this.sendToChat({username: mes.username, content: encAnswer, isServer: false});
            this.sendToChat({
                username: "Server",
                content: format(ANNOUNCEMENT, mes.username),
                isServer: true});
        } else {
            this.sendToChat({username: mes.username, content: mes.content, isServer: false});
        }
    }

    protected handlePoints(): void {
        // Give points to guesser
        // pts = time_left * (1 - ratio_found)
        this.userWithCorrectAns.forEach(u => {
            const pts = this.userMapPoints.get(u.username) as number;
            const newPts = Math.floor(u.time * u.ratio);
            this.userMapPoints.set(u.username, pts + newPts);
        });

        // Give points to drawer
        // pts = 20 * ratio_found
        const drawName = this.users[this.drawPtr].username;
        const drawPts = this.userMapPoints.get(drawName) as number;
        const drawNewPts = Math.floor(INIT_DRAW_PTS * this.calculateRatio());
        this.userMapPoints.set(drawName, drawNewPts + drawPts);
    }

    private attributeRoles(): void {
        let user = this.users[this.drawPtr];

        while (this.checkIfUserIsDC(user.username)) {

            // if player is disconnect, increment drawer pointer
            if (++this.drawPtr >= this.users.length) {
                clearInterval(this.curArenaInterval);
                throw new Error("Everyone has drawn once");
            }
            user = this.users[this.drawPtr];
        }

        this.updateDrawerRole(user);
    }
    
    private updateDrawerRole(user: IUser): void {
        this.socketServer.to(this.room).emit("drawer-update", user.username);
    }

    private sendToChat(obj: IGameplayAnnouncement): void {
        this.socketServer.to(this.room).emit("game-chat", obj);
    }

    private checkIfUserIsDC(username: string): boolean {
        return this.dcPlayer.includes(username);
    }

    private calculateRatio(): number {
        return Math.floor(this.userWithCorrectAns.length / this.users.length);
    }

}