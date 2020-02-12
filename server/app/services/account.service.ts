import { injectable, inject } from "inversify";
import { DatabaseService } from "./database.service";
import { IRegistration, IStatus, ILogin } from "../interfaces/communication";
import { UserManagerService } from "./user-manager.service"; 
import Types from '../types';

@injectable()
export class AccountService {

    public constructor(
        @inject(Types.DatabaseService) private database: DatabaseService,
        @inject(Types.UserManagerService) private userServ: UserManagerService) {}

    public async register(registration: IRegistration): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully registered account."
        };

        try {
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

    private verifyUserIsLogged(username: string): boolean {
        const users = this.userServ.getUsers();        
        return users.some((user) => user === username);
    }

}