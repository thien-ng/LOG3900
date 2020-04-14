import { IGameplayAnnouncement } from '../../../interfaces/game';
import { Taunt } from './taunts';
import { Bot } from "./bot";

import * as io from 'socket.io';

export class MeanBot extends Bot {

    constructor(socket: io.Server, username: string) {
        super(socket, username);
        this.taunts = Taunt.mean;
    }

    public launchTauntStart(room: string, gameMessages: IGameplayAnnouncement[]): void {
        const announcement: IGameplayAnnouncement = {
            username: this.username,
            content: "Ur all trash, EZ win today boiz!",
            isServer: false,
        };
        gameMessages.push(announcement);
        this.socket.to(room).emit("game-chat", announcement);
    }

}
