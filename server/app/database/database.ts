import { injectable } from "inversify";
import * as pg from "pg";

@injectable()
export class DatabaseService {

    public connectionConfig: pg.ConnectionConfig = {
        user: "rfbpejtdulvzja",
        database: "dfcm6enu6paah0",
        password: "f0c366c68eec5e65232cad1820aceba731d8cc4d650b40e4d60e81a4bde2bc31",
        port: 5432,
        host: "ec2-174-129-253-86.compute-1.amazonaws.com",
        ssl: true,
        keepAlive : true
    };

    protected pool: pg.Pool = new pg.Pool(this.connectionConfig);

    public constructor() {
        this.pool.connect().catch();
    }

}