import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw, IDrawing } from "../../interfaces/game";
import { IGameRule } from "../../interfaces/rule";

import * as io from 'socket.io';

export abstract class Arena {

    protected socketServer: io.Server;
    protected users: IUser[];
    protected rules: IGameRule[];
    protected room: string;
    protected dcPlayer: string[];
    private size: number;

    public constructor(users: IUser[], size: number, room: string, io: io.Server, rules: IGameRule[]) {
        this.users = users;
        this.room = room;
        this.size = size;
        this.socketServer = io;
        this.rules = rules;
        
        if (this.size) {}

        this.dcPlayer = [];
    }
    
    public abstract start(): void;
    public abstract receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw): void;
    
    protected abstract handleGameplayChat(mes: IGameplayChat): void;

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
            this.socketServer.to(u.socketId).emit("game-over");
        });
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

}