import * as chai from "chai";
import * as spies from "chai-spies";
import { AccountService } from "../services/account.service";
import Types from '../types';
import { container } from "../inversify.config";
import { IStatus } from "../interfaces/communication";

chai.use(spies);

describe("AccountService", () => {

    let service: AccountService;

    beforeEach(() => {
        container.snapshot();
        service = container.get<AccountService>(Types.AccountService);
    });

    afterEach(() => {
        container.restore();
    });

    it("Should return status 400 register fail", async () => {
        // given
        chai.spy.on(service["database"], "registerAccount", () => { throw new Error("potatoes") });

        //when
        const status: IStatus = await service.register({ username: "username", password: "password", firstName: "first", lastName: "last" });

        //then
        chai.expect(status.status).to.be.equal(400);
        chai.expect(status.message).to.be.equal("potatoes");
    });

    it("Should return status 200 when register pass", async () => {
        //given
        chai.spy.on(service["database"], "registerAccount", () => { null });

        //when
        const status: IStatus = await service.register({ username: "username", password: "password", firstName: "first", lastName: "last" });

        //then
        chai.expect(status.status).to.be.equal(200);
        chai.expect(status.message).to.be.equal("Succesfully registered account.");
    });

    it("Should return status 400 when login fail", async () => {
        //given
        chai.spy.on(service["database"], "loginAccount", () => { throw new Error("potatoes") });

        //when
        const status: IStatus = await service.login({ username: "username", password: "password" });

        //then
        chai.expect(status.status).to.be.equal(400);
        chai.expect(status.message).to.be.equal("potatoes");
    });

    it("Should return status 200 when login pass", async () => {
        //given
        chai.spy.on(service["database"], "loginAccount", () => { null });

        //when
        const status: IStatus = await service.login({ username: "username", password: "password" });

        //then
        chai.expect(status.status).to.be.equal(200);
        chai.expect(status.message).to.be.equal("Succesfully logged in.");
    });

});
