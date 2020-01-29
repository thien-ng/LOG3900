import { injectable } from "inversify";
import * as io from 'socket.io';

@injectable()
export class UserManagerService {

    private usersMap: Map<string, io.Socket>;

    public constructor() {
        this.usersMap = new Map<string, io.Socket>();
    }

    public addUser(username: string, socket: io.Socket): void {
        this.usersMap.set(username, socket);
    }

    public deleteUser(username: string): void {
        this.usersMap.delete(username);
    }

}