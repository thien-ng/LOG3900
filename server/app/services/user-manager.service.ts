import { injectable } from "inversify";
import { IUser } from '../interfaces/user-manager';

@injectable()
export class UserManagerService {

    private users: IUser[];

    public constructor() {
        this.users = [];
    }

    public addUser(user: IUser): void {
        this.users.push(user);
    }

    public deleteUser(username: string): void {
        this.users = this.users.filter((obj: IUser) => obj.username !== username);
    }

}