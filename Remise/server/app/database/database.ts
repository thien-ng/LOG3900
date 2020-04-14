import { injectable } from "inversify";
import * as pg from "pg";

@injectable()
export class DatabaseService {

    public connectionConfig: pg.ConnectionConfig = {
        ssl: true,
        keepAlive: true,
        //real database
        user: "rfbpejtdulvzja",
        database: "dfcm6enu6paah0",
        password: "f0c366c68eec5e65232cad1820aceba731d8cc4d650b40e4d60e81a4bde2bc31",
        port: 5432,
        host: "ec2-174-129-253-86.compute-1.amazonaws.com"
        /*
        //la DB pour tester:
        host: "ec2-34-200-116-132.compute-1.amazonaws.com",
        database: "d2bnr3icnde707",
        user: "tbuclxzktiekbg",
        port: 5432,
        password: "055a4ede49a1639ae9f536d88f03d5533b966c682f5e3791749c6c6c8db8d033"
        */

    };

    protected pool: pg.Pool = new pg.Pool(this.connectionConfig);

    public constructor() {
        this.pool.connect().catch();
    }

}
