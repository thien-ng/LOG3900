import { injectable, inject } from "inversify";
import { DatabaseService } from "./database.service";
import Types from '../types';
import { IUser } from "../interfaces/user-manager";
import * as pg from "pg";

@injectable()
export class ChatService {

    private channelMapUsersList: Map<number, IUser[]>;

    public constructor(@inject(Types.DatabaseService) private db: DatabaseService) {
        this.channelMapUsersList = new Map<number, IUser[]>();
    }

    public addUserToChannelMap(user: IUser): void {
        this.db.getChannelsWithAccountId(user.username).then((result: pg.QueryResult) => {
            const channelList: number[] = result.rows;

            channelList.forEach((chan_id: number) => {
                let list = this.channelMapUsersList.get(chan_id);
                if (list) {
                    list.push(user);
                } else {
                    list = [];
                    
                }
            });
            
        });
    }


}