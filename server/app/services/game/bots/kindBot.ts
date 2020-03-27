import { IGameplayAnnouncement } from '../../../interfaces/game';
import { Taunt } from './taunts';
import { Bot } from "./bot";

import * as io from 'socket.io';

export class KindBot extends Bot {

    constructor(socket: io.Server, username: string) {
        super(socket, username);
        this.taunts = Taunt.kind;
    }
    
    public launchTauntStart(room: string): void {
        const announcement: IGameplayAnnouncement = {
            username: this.username,
            content: "Even if I'm going to win, let's try to have fun!",
            isServer: false,
        };
        this.socket.to(room).emit("game-chat", announcement);
    }

}
