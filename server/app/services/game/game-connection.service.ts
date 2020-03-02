import { injectable } from "inversify";

import * as io from 'socket.io';

@injectable()
export class GameConnection {

    protected socketServer: io.Server;

    public initSocketServer(socketServer: io.Server): void {
        this.socketServer = socketServer;     
    }

}