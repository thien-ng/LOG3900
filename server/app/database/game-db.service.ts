import { injectable } from "inversify";
import { DatabaseService } from "../database/database";
import * as pg from "pg";

@injectable()
export class GameDbService extends DatabaseService {

}