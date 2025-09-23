const feedback = [
    { id:1, name:"Dr. Smith", email:"smith@hospital.com", contact:"+94 77 3456789", rating:4, category:"Doctor", date:"2025-09-18", message:"Knowledgeable but wait time long", status:"Rejected" },
    { id:2, name:"User1", email:"u1@example.com", contact:"+94 11 1111111", rating:5, category:"Doctor", date:"2025-09-01", message:"Great!", status:"Pending" },
    { id:3, name:"Mary Silva", email:"mary@nurses.com", contact:"+94 75 4567890", rating:3, category:"Nurse", date:"2025-09-20", message:"Caring but slow", status:"Pending" },
    { id:4, name:"User2", email:"u2@example.com", contact:"+94 11 2222222", rating:4, category:"Nurse", date:"2025-09-02", message:"Good service", status:"Approved" },
    { id:5, name:"John Doe", email:"john@example.com", contact:"+94 71 2345678", rating:5, category:"Patient", date:"2025-09-16", message:"Very satisfied", status:"Approved" },
    { id:6, name:"City General Hospital", email:"cityhospital@example.com", contact:"+94 11 2345678", rating:4, category:"Hospital", date:"2025-09-15", message:"Good facilities", status:"Pending" }
];

const categories = ["Doctor","Nurse","Hospital","Patient"];
const ratingCounts = [0,0,0,0,0];
const categoryCounts = { Doctor:0, Nurse:0, Hospital:0, Patient:0 };
const ratingsByCategory = categories.map(cat => {
    const obj = { category:cat, "1":0,"2":0,"3":0,"4":0,"5":0 };
    feedback.forEach(f => {
        if(f.category === cat){
            obj[f.rating]++;
            categoryCounts[cat]++;
            ratingCounts[f.rating-1]++;
        }
    });
    return obj;
});

// Charts
new Chart(document.getElementById("ratingChart"), {
    type:"pie",
    data:{ labels:["1★","2★","3★","4★","5★"], datasets:[{ data:ratingCounts, backgroundColor:["#cce0ff","#99c2ff","#66a3ff","#3385ff","#0055cc"] }] },
    options:{ plugins:{ legend:{ position:'bottom', labels:{ color:'#003366' } } } }
});

new Chart(document.getElementById("categoryChart"), {
    type:"pie",
    data:{ labels:Object.keys(categoryCounts), datasets:[{ data:Object.values(categoryCounts), backgroundColor:["#0055cc","#3385ff","#66a3ff","#99c2ff"] }] },
    options:{ plugins:{ legend:{ position:'bottom', labels:{ color:'#003366' } } } }
});

new Chart(document.getElementById("ratingsByCategoryChart"), {
    type:"bar",
    data:{
        labels:categories,
        datasets:[
            { label:"1★", data:ratingsByCategory.map(c=>c["1"]), backgroundColor:"#cce0ff" },
            { label:"2★", data:ratingsByCategory.map(c=>c["2"]), backgroundColor:"#99c2ff" },
            { label:"3★", data:ratingsByCategory.map(c=>c["3"]), backgroundColor:"#66a3ff" },
            { label:"4★", data:ratingsByCategory.map(c=>c["4"]), backgroundColor:"#3385ff" },
            { label:"5★", data:ratingsByCategory.map(c=>c["5"]), backgroundColor:"#0055cc" }
        ]
    },
    options:{
        responsive:true,
        plugins:{ legend:{ position:'bottom', labels:{ color:'#003366' } } },
        scales:{
            y:{ beginAtZero:true, stacked:true, ticks:{ color:'#003366' } },
            x:{ stacked:true, ticks:{ color:'#003366' } }
        }
    }
});

const statusCounts = { Approved:0, Pending:0, Rejected:0 };
feedback.forEach(f => statusCounts[f.status]++);
new Chart(document.getElementById("extraChart"), {
    type:"doughnut",
    data:{
        labels:Object.keys(statusCounts),
        datasets:[{ data:Object.values(statusCounts), backgroundColor:["#007bff","#66b0ff","#003366"] }]
    },
    options:{ plugins:{ legend:{ position:'bottom', labels:{ color:'#003366' } } } }
});

const container = document.getElementById("tables-container");

// Render Tables Function
function renderTables(filter="all"){
    container.innerHTML = ""; // clear old content
    categories.forEach(cat => {
        let catFeedback = feedback.filter(f=>f.category===cat);

        // Apply filter
        if(filter==="approved"){
            catFeedback = catFeedback.filter(f=>f.status==="Approved");
        }

        if(catFeedback.length===0) return; // skip empty categories

        const div = document.createElement("div");
        div.innerHTML = `<h2>${cat}</h2>`;
        const table = document.createElement("table");
        table.innerHTML = `
    <thead>
      <tr>
        <th>Name</th><th>Email</th><th>Contact</th><th>Rating</th>
        <th>Message</th><th>Date</th><th>Status</th><th>Actions</th>
      </tr>
    </thead>
    <tbody>
      ${catFeedback.map(f=>`
        <tr data-id="${f.id}">
          <td>${f.name}</td><td>${f.email}</td><td>${f.contact}</td><td>${f.rating}</td>
          <td>${f.message}</td><td>${f.date}</td>
          <td><span class="status ${f.status.toLowerCase()}">${f.status}</span></td>
          <td><button class="btn action-btn">${f.status==="Approved"?"Unapprove":"Approve"}</button></td>
        </tr>
      `).join("")}
    </tbody>
  `;
        div.appendChild(table);
        container.appendChild(div);
    });

    // Attach button logic again after rendering
    document.querySelectorAll(".action-btn").forEach(btn=>{
        btn.addEventListener("click", function(){
            const row = this.closest("tr");
            const statusSpan = row.querySelector(".status");
            const id = parseInt(row.dataset.id);
            const item = feedback.find(f => f.id===id);

            if(this.textContent==="Approve"){
                statusSpan.textContent="Approved";
                statusSpan.className="status approved";
                this.textContent="Unapprove";
                item.status = "Approved";
            } else {
                statusSpan.textContent="Pending";
                statusSpan.className="status pending";
                this.textContent="Approve";
                item.status = "Pending";
            }
        });
    });
}

// Default view: All Reviews
renderTables("all");

// Filter Button Logic
document.querySelectorAll(".filter-btn").forEach(btn=>{
    btn.addEventListener("click", function(){
        document.querySelectorAll(".filter-btn").forEach(b=>b.classList.remove("active"));
        this.classList.add("active");
        const filter = this.dataset.filter;
        renderTables(filter);
    });
});
