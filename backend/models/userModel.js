import mongoose from "mongoose"

const userSchema = new mongoose.Schema({
    user_name: {
        type: String,
        required: true,
    },
    password: {
        type: String,
        required: true,
    },
    email: {
        type: String,
    },
    contact_no:{
        type: Number, 
        required: true,
        unique: true,
    },
    userType: {
        type: String, 
        default: "Visitor",
    }
}, {
    timestamp: true,
})

const User = mongoose.model("User", userSchema)

export default User