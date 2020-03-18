import { Arena } from "./arena";
import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw, IGameplayAnnouncement } from "../../interfaces/game";
import { IGameRule } from "../../interfaces/rule";

import * as io from 'socket.io';
const StringBuilder = require("string-builder");
const format = require('string-format');

const ANNOUNCEMENT = "{0} has found the answer";

export class ArenaFfa extends Arena {

    private drawPtr: number;
    private curArenaInterval: NodeJS.Timeout;
    private curRule: IGameRule;

    public constructor(users: IUser[], size: number, room: string, io: io.Server, rules: IGameRule[]) {
        super(users, size, room, io, rules)
        
        this.drawPtr = 0;
        this.curRule = this.rules[0];
    }

    public start(): void {
        console.log("[Debug] Starting arena", this.room);    
    }

    private startSubGame(): void {

        this.attributeRoles();
        this.chooseRandomRule();

        let timer = 0;
        this.curArenaInterval = setInterval(() => {
            console.log("[Debug] Timer is: ", timer += 1000);
            
            this.socketServer.to(this.room).emit("timer", timer/1000);

            if (timer >= 30000) {
                clearInterval(this.curArenaInterval);

                if (this.drawPtr >= this.users.length) {
                    // Handle end of game
                    this.end();
                } else {
                    // Make next person to draw
                    this.startSubGame();
                }
            }

        }, 1000);
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

    private encryptAnswer(ans: string): string {
        let enc = new StringBuilder();
        for (let i = 0; i < ans.length; i++) {
            enc.append("*");
        }
        return enc;
    }

    private isRightAnswer(ans: string): boolean {
        return this.curRule.solution === ans;
    }

    private chooseRandomRule(): void {
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

    private attributeRoles(): void {
        let user = this.users[this.drawPtr];

        while (this.checkIfUserIsDC(user.username)) {

            // if player is disconnect, increment drawer pointer
            if (++this.drawPtr >= this.users.length) {
                throw new Error("Everyone has drawn once");
            }
            user = this.users[this.drawPtr];
        }

        this.updateDrawer(user);
    }
    
    private updateDrawer(user: IUser): void {
        this.socketServer.to(this.room).emit("drawer-update", user.username);
    }

    private sendToChat(obj: IGameplayAnnouncement): void {
        this.socketServer.to(this.room).emit("game-chat", obj);
    }

    private checkIfUserIsDC(username: string): boolean {
        return this.dcPlayer.includes(username);
    }

}