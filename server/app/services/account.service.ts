import { injectable, inject } from "inversify";
import { IRegistration, IStatus, ILogin } from "../interfaces/communication";
import { UserManagerService } from "./user-manager.service"; 
import { AccountDbService } from "../database/account-db.service";
import Types from '../types';

@injectable()
export class AccountService {

    public constructor(
        @inject(Types.AccountDbService) private database: AccountDbService,
        @inject(Types.UserManagerService) private userServ: UserManagerService) {}

    public async register(registration: IRegistration): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully registered account."
        };

        try {
            this.verifyRegistration(registration);
            await this.database.registerAccount(registration);
        } catch(e) {
            result.status = 400;
            result.message = e.message;
        }

        return result;
    }

    public async login(login: ILogin): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully logged in."
        };

        try {
            this.verifyLogin(login);
            if (this.verifyUserIsLogged(login.username)) {
                throw new Error(`${login.username} is already logged in.`);
            }
            this.userServ.addUser(login.username);
            await this.database.loginAccount(login);

        } catch(e) {
            result.status = 400
            result.message = e.message;
        }

        return result;
    }

    public getOnlineUsers(): string[] {
        return this.userServ.getUsers();
    }

    private verifyUserIsLogged(username: string): boolean {
        const users = this.userServ.getUsers();        
        return users.some((user) => user === username);
    }

    private verifyRegistration(regis: IRegistration): void {
        if (regis.username.length < 1 || regis.username.length > 20) {
            throw new Error("username length should be between 1 and 20");
        }
        if (regis.password.length < 1 || regis.password.length > 20) {
            throw new Error("password length should be between 1 and 20");
        }
    }

    private verifyLogin(login: ILogin): void {
        if (login.username.length < 1 || login.username.length > 20) {
            throw new Error("username length should be between 1 and 20");
        }
        if (login.password.length < 1 || login.password.length > 20) {
            throw new Error("password length should be between 1 and 20");
        }
    }

}