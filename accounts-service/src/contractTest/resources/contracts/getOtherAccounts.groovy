package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should getAllAccounts for transfer by ID")
    request {
        method GET()
        urlPath("/accounts/3") {}
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                [
                        [
                            id: "1",
                            phone: "89996665522",
                            name: "Test",
                            surname: "Test"
                        ],
                        [

                            id: "2",
                            phone: "89106665522",
                            name: "Ne test",
                            surname: "Ne test"
                        ]
                ]
        )
    }
}