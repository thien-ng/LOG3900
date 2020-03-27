import { IGameplayAnnouncement } from '../../../interfaces/game';
import { Taunt } from './taunts';
import { Bot } from "./bot";

import * as io from 'socket.io';

export class HumourBot extends Bot {

    constructor(socket: io.Server, username: string) {
        super(socket, username);
        this.taunts = Taunt.humour;
    }

    public launchTauntStart(room: string): void {
        const announcement: IGameplayAnnouncement = {
            username: this.username,
            content: "You're gonna lose, just like yo mamma!",
            isServer: false,
        };
        this.socket.to(room).emit("game-chat", announcement);
    }

}
