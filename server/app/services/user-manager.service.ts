import { injectable } from "inversify";
import * as ws from 'ws';

@injectable()
export class UserManagerService {

    private usersMap: Map<string, ws>;

    public constructor() {
        this.usersMap = new Map<string, ws>();
    }

    public addUser(username: string, socket: ws): void {
        this.usersMap.set(username, socket);
    }

    public deleteUser(username: string): void {
        this.usersMap.delete(username);
    }

}