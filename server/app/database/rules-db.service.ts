import { injectable } from 'inversify';
import { Collection, MongoClient, MongoClientOptions } from 'mongodb';
import { IGameRule } from "../interfaces/rule";

import 'reflect-metadata';

const DATABASE_URL = 'mongodb+srv://admin:admin@cluster0-bt4kd.mongodb.net/test?retryWrites=true&w=majority';
const DATABASE_NAME = 'database';
const DATABASE_COLLECTION = 'cards';

@injectable()
export class RulesDbService {

    private collection: Collection<IGameRule>;

    private options: MongoClientOptions = {
        useNewUrlParser : true,
        useUnifiedTopology : true
    };

    public constructor() {
        MongoClient.connect(DATABASE_URL, this.options).then(client => {
            this.collection = client.db(DATABASE_NAME).collection(DATABASE_COLLECTION);            
        })
        .catch(() => {
            console.error('CONNECTION ERROR. EXITING PROCESS');
            process.exit(1);
        });
    }

    public async addRule(rule: IGameRule): Promise<void> {
        if (this.validateRule(rule)) {
            await this.collection.insertOne(rule).catch(e => {
                throw e;
            });
        } else {
            throw new Error("Invalid");
        }
    }

    public async getRules(): Promise<IGameRule[]> {
        return this.collection.find({}).toArray().then((rules) => {
            return rules;
        }).catch((error: Error) => {
            throw error;
        });
    }

    private validateRule(rule: IGameRule): boolean {
        // TODO add vallidation of card before adding to db
        // add validation when we know what is needed for a card
        return true
    }

}