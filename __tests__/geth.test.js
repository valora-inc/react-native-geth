import Geth from '../src/geth'

// jest.mock('../src/geth')

// const geth = new Geth({})

describe('Test Geth Class', () => {

  test('Check instance of a Geth', () => {
    expect(new Geth({})).toBeInstanceOf(Geth)
  })

  /*
  test('Expect methode start() return true', async () => {
    expect.assertions(1)
    const start = await geth.start()
    expect(start).toBe(true)
  })
  */
})
