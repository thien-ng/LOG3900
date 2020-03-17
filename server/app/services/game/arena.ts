import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw, IDrawing } from "../../interfaces/game";

import * as io from 'socket.io';

export abstract class Arena {

    private socketServer: io.Server;
    private users: IUser[];
    private room: string;
    private size: number;
    // TODO add attribute game rule search by the uuid

    public constructor(users: IUser[], size: number, room: string, io: io.Server) {
        this.users = users;
        this.room = room;
        this.size = size;
        this.socketServer = io;
        
        if (this.users || this.size || this.room || this.socketServer) {}
    }
    
    public start(): void {
        let timer = 0;
        const test = setInterval(() => {
            console.log("[Debug] Timer is: ", timer += 1000);
            if (timer > 60000) {
                clearInterval(test);
                this.end();
            }
        }, 1000); //1 minute    
    }

    private end(): void {
        console.log("[Debug] End routine");
        this.users.forEach(u => {
            this.socketServer.to(u.socketId).emit("game-over");
        });
    }

    public receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw): void {
        if (this.isDraw(mes)) {
            this.users.forEach(u => {
                if (u.username != mes.username)
                    socket.to(this.room).emit("draw", this.mapToDrawing(mes))
            });
        }
    }

    public disconnectPlayer(username: string): void {
        const user = this.users.find(u => {return u.username === username});
        user?.socket?.leave(this.room);
    }

    private mapToDrawing(draw: IGameplayDraw): IDrawing {
        return {
            startPosX:  draw.startPosX,
            startPosY:  draw.startPosY,
            endPosX:    draw.endPosX,
            endPosY:    draw.endPosY,
            color:      draw.color,
            width:      draw.width,
        }
    }

    private isDraw(mes: IGameplayChat | IGameplayDraw): mes is IGameplayDraw {
        return "startPosX" in mes;
    }

}