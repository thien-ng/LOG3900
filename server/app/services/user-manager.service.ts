import { injectable } from "inversify";
import { IUser } from '../interfaces/user-manager';

@injectable()
export class UserManagerService {

    private users: IUser[];

    public constructor() {
        this.users = [];
    }

    public getUsers(): IUser[] {
        return this.users;
    }

    public getUsersByName(name: string): IUser | undefined {
        return this.users.find(u => u.username === name);
    }

    public addUser(user: IUser): void {
        this.users.push(user);
    }

    public deleteUser(username: string): void {
        this.users = this.users.filter((user: IUser) => user.username !== username);
    }

    public checkIfUserIsOnline(user: string): boolean {        
        return this.users.some((el) => {return el.username === user});
    }

    public getOnlineUsers(): String[] {
        const users: String[] = []
        this.users.forEach(u => {users.push(u.username)})
        return users
    }

}