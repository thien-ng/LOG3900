import { injectable, inject } from "inversify";
import { DatabaseService } from "./database.service";
import { IRegistration } from "../interfaces/communication";
import Types from '../types';

@injectable()
export class AccountService {

    public constructor(@inject(Types.DatabaseService) private databaseService: DatabaseService) {
        
    }

    public register(registration: IRegistration): string {


        return '';
    }

}