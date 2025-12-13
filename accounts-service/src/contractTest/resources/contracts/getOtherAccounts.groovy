package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should getAllAccounts for transfer by ID")
    request {
        method GET()
        url("/accounts/3")
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                [
                        [id: "1", ownerId: "2", currency: "RUB", name: "Test", surname: "Test", phone: "89996665522"] as Map,
                        [id: "2", ownerId: "2", currency: "EUR", name: "Test", surname: "Test", phone: "89996665522"] as Map
                ]
        )
    }
}