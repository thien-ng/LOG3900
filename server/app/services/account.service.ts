import { injectable, inject } from "inversify";
<<<<<<< HEAD
import { IRegistration, IStatus, ILogin } from "../interfaces/communication";
=======
import { IRegistration, IStatus, ILogin, IinfoUser } from "../interfaces/communication";
import { UserManagerService } from "./user-manager.service";
>>>>>>> D:ajout des info users, get info user et utils folder
import { AccountDbService } from "../database/account-db.service";
import * as pg from "pg";
import Types from '../types';

@injectable()
export class AccountService {

    public constructor(
        @inject(Types.AccountDbService) private database: AccountDbService) { }

    public async register(registration: IRegistration): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully registered account."
        };

        try {
            this.verifyRegistration(registration);
            await this.database.registerAccount(registration);
        } catch (e) {
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
            await this.database.loginAccount(login);
            this.userServ.addUser(login.username);

        } catch (e) {
            result.status = 400
            result.message = e.message;
        }

        return result;
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

    public async getUserInfo(username: string): Promise<IinfoUser> {
        return this.database.getUserInfo(username).then((result: pg.QueryResult) => {
            const res: IinfoUser[] = result.rows.map((row: any) => ({ username: row.username, last_name: row.last_name, first_name: row.first_name }));
            return res[0];
        });
    }

}
