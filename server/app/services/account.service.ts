import { injectable, inject } from "inversify";
import { IRegistration, IStatus, ILogin, IInfoUser } from "../interfaces/communication";
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
        if (regis.firstName.length < 1 || regis.firstName.length > 100) {
            throw new Error("first name length should be between 1 and 100");
        }
        if (regis.lastName.length < 1 || regis.lastName.length > 100) {
            throw new Error("last name length should be between 1 and 100");
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

    public async getUserInfo(username: string): Promise<IInfoUser> {
        return this.database.getAccountInfoByName(username).then((result: pg.QueryResult) => {
            const res: IInfoUser[] = result.rows.map((row: any) => ({ username: row.out_username, lastName: row.out_lastname, firstName: row.out_firstname }));
            return res[0];
        }).catch((e) => {
            return e;
        });
    }

}
