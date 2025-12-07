package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should update currencies rates")
    request {
        method GET()
        urlPath("/blocker/check")
    }
    response {
        status OK()
        headers { contentType(applicationJson()) }
        body(
                true
        )
    }
}