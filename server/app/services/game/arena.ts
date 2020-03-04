import { IUser } from "../../interfaces/user-manager";
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
    
}