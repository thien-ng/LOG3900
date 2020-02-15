import { injectable } from "inversify";

@injectable()
export class UserManagerService {

    private users: string[];

    public constructor() {
        this.users = [];
    }

    public getUsers(): string[] {
        return this.users;
    }

    public addUser(user: string): void {
        this.users.push(user);
    }

    public deleteUser(username: string): void {
        this.users = this.users.filter((user: string) => user !== username);
    }

}