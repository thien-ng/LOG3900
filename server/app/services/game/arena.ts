import { IUser } from "../../interfaces/user-manager";
import { IGameplayChat, IGameplayDraw } from "../../interfaces/game";

import * as io from 'socket.io';

export class Arena {

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
        console.log("End routine");
        
        this.users.forEach(u => {
            this.socketServer.to(u.socketId).emit("game-over");
        });
    }

    public receiveInfo(mes: IGameplayChat | IGameplayDraw): void {
        if (this.isDraw(mes)) {
            // TODO emit to everyone else the drawings
        }
    }

    private isDraw(mes: IGameplayChat | IGameplayDraw): mes is IGameplayDraw {
        return "pos_x" in mes;
    }

}