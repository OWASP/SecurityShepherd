
//-- ======================================================
//-- NoSQL Injection One
//-- ======================================================

//REMOVE db = db.getSiblingDB('shepherdGames')
//REMOVE db.dropDatabase()
//REMOVE db = db.getSiblingDB('shepherdGames')

db.createUser({ "user": "gamer1", "pwd": "$ecSh3pdb", "roles": [{"role": "read", "db": "shepherdGames" }] })
db.createCollection("gamer")

db.gamer.insert
([
    { _id : "77b6eab74151d73c2efe561737981a1fbb2291fe3643680f5824e47f69fc9985", name : "Omar", address : "Baltimore" },
    { _id : "fd1b4a1d16714cee9c1320f3e7465792010d0911075b7c1002071d48e68e19b4", name : "Stringer", address : "Baltimore" },
    { _id : "b43c05a8166b5bbc5e2baebbbc84a71f83fd7cac01dd5d23bb6e003a95d60b7c", name : "Jimmy", address : "Baltimore" },
    { _id : "c09f32d4c3dd5b75f04108e5ffc9226cd8840288a62bdaf9dc65828ab6eaf86a", name : "Marlo", address : "Baltimore" },
    { _id : "b6c02d3459803a3e3bf99a8c5a20e2f661a8296bbeec858110d3597aaa9b830a", name : "Clayton", address : "Baltimore" },
    { _id : "4b54501900f31f40832a67accf8ab97150a7cb7938d814dffd534e64b1e658bd", name : "Lester", address : "Baltimore" }

])
