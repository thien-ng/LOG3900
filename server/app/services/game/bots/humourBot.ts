import { Taunt } from './taunts';
import { Bot } from "./bot";

import * as io from 'socket.io';

export class HumourBot extends Bot {

    constructor(socket: io.Server, username: string) {
        super(socket, username);
        this.taunts = Taunt.humour;
    }

}
